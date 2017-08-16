package controllers

import java.nio.file.Files
import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import store.Store
import store.Store._

import scala.concurrent.duration._

import protocol.Protocol

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
        NotFound(Json.obj("error" -> s"Request $id not found"))
      case RequestData(_, from, to) =>
        val contentType = Some("application/msword")

        val entity = Protocol.filesToMultipart(
          ("from", "from.doc", contentType, from),
          ("to", "to.doc", contentType, to)
        )

        Result (
          header = ResponseHeader(200, Map.empty),
          entity
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

  def queryResponse(id: UUID) = Action.async { implicit request =>
    (store ? GetResponse(id)).map {
      case Store.NotFound(_) =>
        NotFound(Json.obj("error" -> s"Request $id not found"))

      case NotCompleted(_) =>
        Accepted("")

      case ResponseData(_, data) =>
        Ok(data).as("text/json")
    }
  }

  def addResponse(id: UUID) = Action.async(parse.byteString) { implicit request =>
    val data = request.body.toArray
    (store ? AddResponse(id, data)).map {
      case Store.NotFound(_) => NotFound(Json.obj("error" -> s"Request $id not found"))

      case ResponseAdded(_) => Ok("")
    }
  }
}
