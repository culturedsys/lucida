package store

import java.time.{Duration, Instant}
import java.util.UUID

import scala.math.Ordering.Implicits._

import akka.actor.{Actor, ActorLogging, Props}

/**
  * The main actor representing the store for requests and responses.
  */
class Store extends Actor with ActorLogging {
  import Store._

  override def receive: Receive = state(Map(), Map(), Map())

  def state(requests: Map[UUID, Timestamped[(Document, Document)]],
            pending: Map[UUID, Timestamped[(Document, Document)]],
            responses: Map[UUID, Timestamped[Array[Byte]]]) : Receive = {
    case AddRequest(from, to) =>
      log.debug("Received AddRequest")
      val id = UUID.randomUUID()
      context.become(state(requests + (id -> Timestamped((from, to))), pending, responses))
      sender ! RequestAdded(id)

    case ListRequests =>
      log.debug("Received ListRequests")
      sender ! RequestList(requests.keys.toSeq)

    case ClaimRequest(id) =>
      log.debug("Received GetRequest")
      sender ! (requests.get(id) match {
        case Some(Timestamped(_, (from, to))) =>
          context.become(state(requests - id,
            pending + (id -> Timestamped((from, to))),
            responses))
          RequestData(id, from, to)
        case None => NotFound(id)
      })

    case AddResponse(id, data) =>
      log.debug("Received AddResponse")
      if (responses.contains(id))
        sender ! ResponseRepeated(id)
      else if (!requests.contains(id) && !pending.contains(id))
        sender ! NotFound(id)
      else {
        context.become(state(requests - id, pending - id, responses + (id -> Timestamped(data))))
        sender ! ResponseAdded(id)
      }

    case GetResponse(id) =>
      log.debug("Received GetResponse")
      if (!responses.contains(id))
        if (!requests.contains(id) && ! pending.contains(id))
          sender ! NotFound(id)
        else
          sender ! NotCompleted(id)
      else
        sender ! ResponseData(id, responses(id).payload)

    case Cleanup(requestAge, pendingAge, responseAge) =>
      log.debug("Received Cleanup")
      val now = Instant.now()

      def isYoungerThan[T](maxAge: Duration): ((Any, Timestamped[T])) => Boolean = {
        case (_, Timestamped(stamp, _)) =>
          Duration.between(stamp, now) < maxAge
      }

      val (newPending, oldPending) = pending.partition(isYoungerThan(pendingAge))

      context.become(state(requests.filter(isYoungerThan(requestAge)) ++ oldPending,
        newPending,
        responses.filter(isYoungerThan(responseAge))))
      sender ! CleanupCompleted
  }
}

case class Document(name: String, data: Array[Byte])

object Store {
  case class AddRequest(from: Document, to: Document)
  case class RequestAdded(id: UUID)

  case object ListRequests
  case class RequestList(ids: Seq[UUID])

  case class ClaimRequest(id: UUID)
  case class RequestData(id: UUID, from: Document, to: Document)

  case class AddResponse(id: UUID, response: Array[Byte])
  case class ResponseAdded(id: UUID)
  case class ResponseRepeated(id: UUID)

  case class GetResponse(id: UUID)
  case class NotCompleted(id: UUID)
  case class ResponseData(id: UUID, data: Array[Byte])

  case class NotFound(id: UUID)

  case class Cleanup(requestAge: Duration, pendingAge: Duration, responseAge: Duration)
  object Cleanup {
    def apply(maxAge: Duration): Cleanup = Cleanup(maxAge, maxAge, maxAge)
  }
  case object CleanupCompleted

  def props: Props = Props(new Store)
}

case class Timestamped[T](timestamp: Instant, payload: T)

object Timestamped {
  def apply[T](payload: T): Timestamped[T] = Timestamped(Instant.now(), payload)
}