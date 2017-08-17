package analysis

import java.net.URI
import java.nio.file.Paths

import model._
import org.apache.commons.io.IOUtils
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{ResponseHeader, Result}
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

  def withFakeServer[T](block: (WSClient, URI, Seq[String]) => T): T = {
    val log = mutable.ArrayBuffer[String]()

    Server.withRouterFromComponents() { components =>
      import play.api.mvc.Results._
      import components.{defaultActionBuilder => Action}

      {
        case GET(p"/requests/") => Action {
          log.append("GET /requests/")
          Ok(Json.toJson(fakeRequests))
        }

        case POST(p"/requests/$id") => Action {
          log.append(s"POST /requests/$id")
          val entity = Protocol.filesToMultipart(fakeRequestData: _*)
          Result (
            header = ResponseHeader(200, Map.empty),
            entity
          )
        }

        case PUT(p"/requests/$id") => Action {
          log.append(s"PUT /requests/$id")
          Ok("")
        }
      }
    } { implicit port =>
      WsTestClient.withClient { client =>
        block(client, new URI(s"http://localhost:$port/"), log)
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
      withFakeServer { (client, base, _) =>
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
      withFakeServer { (client, base, _) => {
          var called = false
          def checkedCompare(from: Seq[Paragraph], to: Seq[Paragraph]) = {
            called = true
            compare(from, to)
          }
          val service = new Service(client, base, checkedCompare)
          Await.result(service.processRequest(URI.create(fakeRequests.head)), timeout)

          called should be(true)
        }
      }
    }

    "upload the result of the comparison" in {
      withFakeServer { (client, base, log) =>
        val service = new Service(client, base, compare)
        val uri = URI.create(fakeRequests.head)
        Await.result(service.processRequest(uri), timeout)

        log.filter { entry =>
          entry.startsWith("PUT") && entry.endsWith(uri.toString)
        } should not be(empty)
      }
    }
  }

  "processAllRequests" should {
    "call the comparison function for each request" in {
      withFakeServer { (client, base, _) =>
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
      withFakeServer { (client, base, log) =>
        val service = new Service(client, base, compare)
        Await.result(service.processAllRequests(), timeout)

        log.filter { entry =>
          entry.startsWith("PUT") && fakeRequests.exists(entry.endsWith)
        } should not be(empty)
      }
    }
  }
}
