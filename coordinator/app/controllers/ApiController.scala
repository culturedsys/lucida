package controllers

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import com.google.inject.Inject
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData.{FilePart, Part}
import play.api.mvc._
import play.core.formatters
import store.Store
import store.Store._

import scala.concurrent.duration._


/**
  * Handles all the API requests.
  */
class ApiController @Inject() (cc: ControllerComponents, system: ActorSystem)
    extends AbstractController(cc) {
  lazy val store = system.actorOf(Store.props)
  implicit val ec = defaultExecutionContext
  implicit val timeout = Timeout(5 seconds)

  def listRequests = Action.async { implicit request =>
    (store ? ListRequests).map {
      case RequestList(ids) =>
        val json = Json.toJson(ids.map(_.toString))
        Ok(json)
    }
  }

  def claimRequest(id: UUID) = Action.async { implicit request =>
    (store ? ClaimRequest(id)).map {
      case Store.NotFound(id) =>
        NotFound(Json.obj("error" -> "Request $id not found"))
      case RequestData(_, from, to) =>
        val boundary = formatters.Multipart.randomBoundary()
        val formatter = formatters.Multipart.format(boundary, StandardCharsets.US_ASCII, 65535)
        val fromPart = FilePart("from", "from.doc", Some("application/msword"),
          Source.single(ByteString.fromArray(from)))
        val toPart = FilePart("to", "to.doc",
          Some("application/msword"), Source.single(ByteString.fromArray(to)))
        val parts = Source(List(fromPart, toPart))
        Result (
          header = ResponseHeader(200, Map.empty),
          HttpEntity.Streamed(parts.via(formatter), None, Some(s"multipart/form-data; boundary=$boundary"))
        )
    }
  }

  def addRequest = Action.async(parse.multipartFormData) { implicit request =>
    val Seq(from, to) = request.body.files.map(fp => Files.readAllBytes(fp.ref.path))
    (store ? AddRequest(from, to)).map {
      case RequestAdded(id) =>
        Ok(Json.toJson(Seq(id)))
    }
  }
}
