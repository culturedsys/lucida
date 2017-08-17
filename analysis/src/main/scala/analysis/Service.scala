package analysis

import java.io.ByteArrayInputStream
import java.net.URI

import model.{DocExtractor, Paragraph, Structure}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSClient
import protocol.Protocol

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success


/**
  * The analysis service that retrieves requests from the coordinator, processes them, and
  * returns responses.
  */
class Service[T](client: WSClient, base: URI,
              compare: (Seq[Paragraph], Seq[Paragraph]) =>
                (Structure[(Paragraph, Change)], Structure[(Paragraph, Change)])
) {

  val requestsBase = base.resolve("requests/")

  def getRequestList(): Future[Seq[URI]] = {
    client.url(requestsBase.toString).get().map { response =>
      response.json.validate[Seq[String]].get.map(uri => requestsBase.resolve(uri))
    }
  }

  def processRequest(request: URI): Future[Unit] = {
    implicit val structureToJson = Serializers.structureToJson

    client.url(requestsBase.resolve(request).toString).post("").flatMap { response =>
      val files = Protocol.multipartToFiles(response.bodyAsBytes.toArray, response.contentType)
      require(files.size == 2)
      val documents = files.map(data => DocExtractor.extract(new ByteArrayInputStream(data)))

      val Seq(Success(from), Success(to)) = documents

      val (fromDiff, toDiff) = compare(from, to)

      client.url(requestsBase.resolve(request).toString)
        .put(Json.arr(Json.toJson(fromDiff), Json.toJson(toDiff)))
    }.map {response =>
      require(response.status == 200)
    }
  }

  def processAllRequests(): Future[Unit] = {
    getRequestList().flatMap(Future.traverse(_) { request =>
      processRequest(request)
    }).map(_ => ())
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
        "change" -> structure.content._2.toString,
        "children" -> Json.arr(structure.children.map(writes))
      )
    }
  }
}