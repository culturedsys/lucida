package analysis

import java.io.ByteArrayInputStream
import java.net.{URI, URL}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.intel.imllib.crf.nlp.CRFModel
import com.typesafe.config.ConfigFactory
import model.{DocExtractor, Paragraph, Structure}
import play.api.Logger
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.{DefaultBodyWritables, WSClient}
import play.api.libs.ws.ahc.AhcWSClient
import play.api.libs.ws.JsonBodyWritables._
import protocol.Protocol

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
  * The analysis service that retrieves requests from the coordinator, processes them, and
  * returns responses.
  */
class Service(client: WSClient, base: URL,
              compare: (Seq[Paragraph], Seq[Paragraph]) =>
                (Structure[(Paragraph, Change)], Structure[(Paragraph, Change)])
) {

  val requestsBase = new URL(base, "requests/")

  def getRequestList(): Future[Seq[URL]] = {
    client.url(requestsBase.toString).get().map { response =>
      response.json.validate[Seq[String]].get.map(url => new URL(requestsBase, url))
    }
  }

  def processRequest(request: String): Future[Unit] = {
    processRequest(new URL(requestsBase, request))
  }

  def processRequest(request: URL): Future[Unit] = {
    implicit val structureToJson = Serializers.structureToJson

    client.url(request.toString).post("").flatMap { response =>
      val files = Protocol.multipartToFiles(response.bodyAsBytes.toArray, response.contentType)
      val documents = files.map { case (filename, data) =>
        DocExtractor.extract(new ByteArrayInputStream(data)).recoverWith {case e =>
          Failure(new IllegalArgumentException(s"Could not read $filename. Is it a Word DOC file? "
            + e.getMessage))
        }
      }

      documents match {
        case Seq(Success(from), Success(to)) =>
          val (fromDiff, toDiff) = compare(from, to)

          client.url(request.toString)
            .put(Json.arr(Json.toJson(fromDiff), Json.toJson(toDiff)))

        case Seq(Failure(e), _) =>
          Future.failed(e)
        case Seq(_, Failure(e)) =>
          Future.failed(e)
        case _ =>
          Future.failed(new IllegalArgumentException("Exactly two documents must be specified"))
      }
    }.recoverWith {
      case e =>
        client.url(request.toString).put(Json.obj("error" -> e.getMessage))
    }.map { response =>
      if (response.status != 200)
        Logger.error(s"Could not post response: ${response.status}")
    }
  }

  def processAllRequests(): Future[Unit] = {
    getRequestList().flatMap(Future.traverse(_) { request =>
      processRequest(request)
    }).map(_ => ())
  }
}

object Service {

  val config = ConfigFactory.load()

  val base = new URL(config.getString("lucida.api.base"))

  implicit val system = ActorSystem("ServiceSystem")
  val client = AhcWSClient()(ActorMaterializer())


  def main(args: Array[String]): Unit = {
    val model = CRFModel.loadStream(getClass.getResourceAsStream("model.data"))

    def compare(from: Seq[Paragraph], to: Seq[Paragraph]) = Analysis.compare(from, to, model)

    val service = new Service(client, base, compare)

    system.scheduler.schedule(0 seconds, 1 seconds)(service.processAllRequests())
  }
}

/**
  * Serializers for the data structures used by the service
  */
object Serializers {
  val structureToJson = new Writes[Structure[(Paragraph, Change)]] {
    def writes(structure: Structure[(Paragraph, Change)]): JsValue = {
      Json.obj(
        "description" -> structure.content._1.description,
        "change" -> structure.content._2.toString.toLowerCase,
        "children" -> structure.children.map(writes)
      )
    }
  }
}