package controllers

import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.UUID
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKitBase}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.WsScalaTestClient
import play.api.libs.Files.SingletonTemporaryFileCreator
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.{Headers, MultipartFormData}
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.FakeRequest
import play.api.test.Helpers._
import store.Store._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Tests for all API resources
  */
class ApiControllerSpec extends WordSpec with Matchers
  with TestKitBase with ImplicitSender
  with WsScalaTestClient  {

  implicit lazy val system = ActorSystem("TestSystem")
  implicit val mat = ActorMaterializer()

  def defaultController =
    new ApiController(stubControllerComponents(playBodyParsers = stubPlayBodyParsers(mat)), system)

  "listRequests" should {
    "respond with an empty array initially" in {
      val result = defaultController.listRequests.apply(FakeRequest(GET, "/"))
      val content: JsValue = contentAsJson(result)
      content.asInstanceOf[JsArray].value should be(empty)
    }

    "respond with an array containing the id of an added Request" in {
      val controller = defaultController
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val result = controller.listRequests.apply(FakeRequest(GET, "/"))
      val content: JsValue = contentAsJson(result)
      content.as[Seq[String]] should contain(id.toString)
    }
  }

  "claimRequest" should {
    val controller = defaultController

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

  "addRequest" should {
    "respond with 400 if the post data does not contain two files" in {
      val controller = defaultController

      val result = controller.addRequest.apply(FakeRequest(POST, "/"))

      status(result) should be(BAD_REQUEST)
    }

    "add a request to the store" in {
      val controller = defaultController

      val fromData = "from".getBytes
      val toData = "to".getBytes

      val fromFile = SingletonTemporaryFileCreator.create()
      Files.write(fromFile.path, fromData)

      val toFile = SingletonTemporaryFileCreator.create()
      Files.write(toFile.path, toData)

      val files = Seq(
        FilePart("from", "from.doc", Some("document/msword"), fromFile),
        FilePart("to", "to.doc", Some("document/msword"), toFile)
      )

      val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq())

      val request = FakeRequest(GET, "/", Headers(), multipartBody)

      val result = controller.addRequest.apply(request)

      val content = contentAsJson(result)
      val id = UUID.fromString(content.as[Seq[String]].head)

      controller.store ! ClaimRequest(id)
      val RequestData(_, from, to) = expectMsgType[RequestData]

      from should equal(fromData)
      to should equal(toData)
    }
  }

  "queryResponse" should {
    "return 404 for an unknown ID" in {
      val result = defaultController.queryResponse(UUID.randomUUID()).apply(FakeRequest())
      status(result) should be (NOT_FOUND)
    }

    "return 202 if the ID is known but not yet completed" in {
      val controller = defaultController
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val result = controller.queryResponse(id).apply(FakeRequest())
      status(result) should be(ACCEPTED)
    }

    "return JSON if the ID has been completed" in {
      val controller = defaultController
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id
      controller.store ! AddResponse(id, "[{},{}]".getBytes(StandardCharsets.UTF_8))
      expectMsgType[ResponseAdded]

      val result = controller.queryResponse(id).apply(FakeRequest())
      status(result) should be(OK)

      contentType(result).get should be("text/json")
    }
  }

  "addResponse" should {
    "return 404 for an unknown ID" in {
      val result = defaultController.addResponse(UUID.randomUUID()).apply(FakeRequest())
      status(result) should be(NOT_FOUND)
    }

    "add the response for a known ID" in {
      val controller = defaultController
      controller.store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val result = controller.addResponse(id)
        .apply(FakeRequest(GET, "/").withBody(Json.arr(Json.obj(), Json.obj())))

      status(result) should be(OK)

      controller.store ! GetResponse(id)
      expectMsgType[ResponseData]
    }
  }
}
