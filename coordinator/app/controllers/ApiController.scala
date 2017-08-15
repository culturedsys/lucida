package controllers

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, Controller, ControllerComponents}
import store.Store
import store.Store.{ListRequests, RequestList}
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

}
