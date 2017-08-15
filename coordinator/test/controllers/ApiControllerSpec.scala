package controllers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.words.ShouldVerb
import org.scalatestplus.play.{PlaySpec, WsScalaTestClient}
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.libs.json.{JsArray, JsValue}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._
import store.Store.{AddRequest, RequestAdded}

/**
  * Tests for all API resources
  */
class ApiControllerSpec extends WordSpec with Matchers
  with TestKitBase with ImplicitSender
  with WsScalaTestClient  {

  implicit lazy val system = ActorSystem("TestSystem")

  "listRequests" should {
    val controller = new ApiController(stubControllerComponents(), system)

    "respond with an empty array initially" in {
      val result = controller.listRequests.apply(FakeRequest(GET, "/"))
      val content: JsValue = contentAsJson(result)
      content.asInstanceOf[JsArray].value should be(empty)
    }

    "respond with an array containing the id of an added Request" in {
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val result = controller.listRequests.apply(FakeRequest(GET, "/"))
      val content: JsValue = contentAsJson(result)
      content.as[Seq[String]] should contain(id.toString)
    }
  }
}
