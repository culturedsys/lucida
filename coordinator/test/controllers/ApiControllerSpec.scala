package controllers

import java.io.InputStream
import java.util.UUID
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.words.ShouldVerb
import org.scalatestplus.play.{PlaySpec, WsScalaTestClient}
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneAppPerTest}
import play.api.libs.json.{JsArray, JsValue}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers._
import store.Store.{AddRequest, RequestAdded}

import scala.concurrent.Await
import scala.concurrent.duration._

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

  "claimRequest" should {
    val controller = new ApiController(stubControllerComponents(), system)

    "respond with Not Found if the id does not match a request" in {
      val result = controller.claimRequest(UUID.randomUUID()).apply(FakeRequest(POST, "/"))
      status(result) should be (NOT_FOUND)
    }

    "respond with Ok if the id does match a request" in {
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val result = controller.claimRequest(id).apply(FakeRequest(POST, "/"))
      status(result) should be(OK)
    }

    val fromData = "from".getBytes
    val toData = "to".getBytes
    controller.store ! AddRequest(fromData, toData)
    val id = expectMsgType[RequestAdded].id

    val result = controller.claimRequest(id).apply(FakeRequest(POST, "/"))

    "respond with type multipart/form-data" in {
      contentType(result) should equal(Some("multipart/form-data"))
    }

    "respond with the request data" in {
      implicit val mat = ActorMaterializer()
      val bytes = contentAsBytes(result)
      val contentType = Await.result(result, 5 seconds).body.contentType
      val mp = new MimeMultipart(new ByteArrayDataSource(bytes.toArray, contentType.get))

      val fromContent = Array.ofDim[Byte](fromData.length)
      mp.getBodyPart(0).getContent.asInstanceOf[InputStream].read(fromContent)
      val toContent = Array.ofDim[Byte](toData.length)
      mp.getBodyPart(1).getContent.asInstanceOf[InputStream].read(toContent)
      fromContent should equal(fromData)
      toContent should equal(toData)
    }
  }
}
