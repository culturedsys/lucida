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

  override def receive: Receive = state(Map(), Map())

  def state(requests: Map[UUID, Timestamped[(Array[Byte], Array[Byte])]],
            responses: Map[UUID, Timestamped[Array[Byte]]]) : Receive = {
    case AddRequest(from, to) =>
      log.debug("Received AddRequest")
      val id = UUID.randomUUID()
      context.become(state(requests + (id -> Timestamped((from, to))), responses))
      sender ! RequestAdded(id)

    case ListRequests =>
      log.debug("Received ListRequests")
      sender ! RequestList(requests.keys.toSeq)

    case GetRequest(id) =>
      log.debug("Received GetRequest")
      sender ! (requests.get(id) match {
        case Some(Timestamped(_, (from, to))) => RequestData(id, from, to)
        case None => NotFound(id)
      })

    case AddResponse(id, data) =>
      log.debug("Received AddResponse")
      if (!requests.contains(id)) {
        sender ! NotFound(id)
      } else {
        context.become(state(requests - id, responses + (id -> Timestamped(data))))
        sender ! ResponseAdded(id)
      }

    case GetResponse(id) =>
      log.debug("Received GetResponse")
      if (!responses.contains(id))
        if (!requests.contains(id))
          sender ! NotFound(id)
        else
          sender ! NotCompleted(id)
      else
        sender ! ResponseData(id, responses(id).payload)

    case Cleanup(maxAge) =>
      log.debug("Received Cleanup")
      val now = Instant.now()

      def isOld[T]: ((Any, Timestamped[T])) => Boolean = {
        case (_, Timestamped(stamp, _)) =>
          Duration.between(stamp, now) < maxAge
      }

      context.become(state(requests.filter(isOld), responses.filter(isOld)))
      sender ! CleanupCompleted
  }
}

object Store {
  case class AddRequest(from: Array[Byte], to: Array[Byte])
  case class RequestAdded(id: UUID)

  case object ListRequests
  case class RequestList(ids: Seq[UUID])

  case class GetRequest(id: UUID)
  case class RequestData(id: UUID, from: Array[Byte], to: Array[Byte])

  case class AddResponse(id: UUID, response: Array[Byte])
  case class ResponseAdded(id: UUID)

  case class GetResponse(id: UUID)
  case class NotCompleted(id: UUID)
  case class ResponseData(id: UUID, data: Array[Byte])

  case class NotFound(id: UUID)

  case class Cleanup(ago: Duration)
  case object CleanupCompleted

  def props: Props = Props(new Store)
}

case class Timestamped[T](timestamp: Instant, payload: T)

object Timestamped {
  def apply[T](payload: T): Timestamped[T] = Timestamped(Instant.now(), payload)
}