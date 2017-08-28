package analysis

import java.net.{URI, URL}
import java.nio.file.Paths

import model._
import org.apache.commons.io.IOUtils
import org.scalatest.{Matchers, WordSpec}
import play.api.http.MediaType.parse
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{BodyParsers, ResponseHeader, Result}
import play.api.test.WsTestClient
import play.core.server.Server
import play.api.routing.sird.{PUT, _}
import protocol.Protocol

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Tests for the service portion of the analyzer
  */
class ServiceSpec extends WordSpec with Matchers {

  val fakeRequests = Seq("fake-id-not-uuid", "a-different-fake-id", "and-another-fake-id")

  val fakeRequestData = Seq(
    ("from", "from.doc", Some("application/msword"),
      IOUtils.toByteArray(classOf[ServiceSpec].getResourceAsStream("sample.doc"))),
    ("to", "to.doc", Some("application/msword"),
      IOUtils.toByteArray(classOf[ServiceSpec].getResourceAsStream("sample-changed-body.doc")))
  )

  val timeout = 1 minute

  def withFakeServer[T](fakeRequestData: Seq[(String, String, Option[String], Array[Byte])])
                       (block: (WSClient, URL, Seq[(String, String, String)]) =>
    T): T = {
    val log = mutable.ArrayBuffer[(String, String, String)]()

    Server.withRouterFromComponents() { components =>
      import play.api.mvc.Results._
      import components.{defaultActionBuilder => Action}

      {
        case GET(p"/requests/") => Action {
          log.append(("GET", "/requests/", ""))
          Ok(Json.toJson(fakeRequests))
        }

        case POST(p"/requests/$id") => Action {
          log.append(("POST", s"/requests/$id", ""))
          val entity = Protocol.filesToMultipart(fakeRequestData: _*)
          Result (
            header = ResponseHeader(200, Map.empty),
            entity
          )
        }

        case PUT(p"/requests/$id") => Action(components.playBodyParsers.json) { request =>
          log.append(("PUT", s"/requests/$id", request.body.toString))
          Ok("")
        }
      }
    } { implicit port =>
      WsTestClient.withClient { client =>
        block(client, new URL(s"http://localhost:$port"), log)
      }
    }
  }

  def compare(from: Seq[Paragraph], to: Seq[Paragraph]) = {
    val fakeParagraph = Paragraph("desc", Seq(), 0, NumberOther, NetOther, 1, Common, false, false,
      false, false)
    (Structure((fakeParagraph, Unchanged)), Structure((fakeParagraph, Unchanged)))
  }

  "getRequestList" should {
    "return the list of request urls" in {
      withFakeServer(fakeRequestData) { (client, base, _) =>
        val service = new Service(client, base, compare)
        val requestList = Await.result(service.getRequestList(), timeout)
        requestList.map { uri =>
          Paths.get(uri.getPath).getFileName.toString
        } should equal(fakeRequests)
      }
    }
  }

  "processRequest" should {
    "call the comparison function with the request data" in {
      withFakeServer(fakeRequestData) { (client, base, _) => {
          var called = false
          def checkedCompare(from: Seq[Paragraph], to: Seq[Paragraph]) = {
            called = true
            compare(from, to)
          }
          val service = new Service(client, base, checkedCompare)
          Await.result(service.processRequest(fakeRequests.head), timeout)

          called should be(true)
        }
      }
    }

    "upload the result of the comparison" in {
      withFakeServer(fakeRequestData) { (client, base, log) =>
        val service = new Service(client, base, compare)
        val uri = fakeRequests.head
        Await.result(service.processRequest(uri), timeout)

        log.filter { case (method, path, _) =>
          method == "PUT" && path.endsWith(uri)
        } should not be(empty)
      }
    }

    "post an error message if only 1 document is uploaded" in {
      withFakeServer(fakeRequestData.take(1)) { (client, base, log) =>
        val service = new Service(client, base, compare)
        val uri = fakeRequests.head
        Await.result(service.processRequest(uri), timeout)

        log.filter { case (method, path, body) =>
          body.length > 0 && ((Json.parse(body) \ "error").isDefined)
        } should not be(empty)
      }
    }

    "post an error message if something other than a word doc is uploaded" in {
      val fakeRequestData = Seq(
        ("from", "from.doc", Some("application/msword"),
          IOUtils.toByteArray(classOf[ServiceSpec].getResourceAsStream("not-a-doc.txt"))),
        ("to", "to.doc", Some("application/msword"),
          IOUtils.toByteArray(classOf[ServiceSpec].getResourceAsStream("sample-changed-body.doc")))
      )
      withFakeServer(fakeRequestData) { (client, base, log) =>
        val service = new Service(client, base, compare)
        val uri = fakeRequests.head
        Await.result(service.processRequest(uri), timeout)

        log.filter { case (method, path, body) =>
          body.length > 0 &&
            ((Json.parse(body) \ "error").get.as[String].startsWith("Could not read from.doc"))
        } should not be(empty)
      }
    }
  }

  "processAllRequests" should {
    "call the comparison function for each request" in {
      withFakeServer(fakeRequestData) { (client, base, _) =>
        var called = 0

        def countCompare(from: Seq[Paragraph], to: Seq[Paragraph]) = {
          called += 1
          compare(from, to)
        }

        val service = new Service(client, base, countCompare)
        Await.result(service.processAllRequests(), timeout)

        called should equal(fakeRequests.length)
      }
    }

    "post results for each request" in {
      withFakeServer(fakeRequestData) { (client, base, log) =>
        val service = new Service(client, base, compare)
        Await.result(service.processAllRequests(), timeout)

        log.filter { case (method, path, _) =>
          method == "PUT" && fakeRequests.exists(path.endsWith)
        } should not be(empty)
      }
    }
  }
}
