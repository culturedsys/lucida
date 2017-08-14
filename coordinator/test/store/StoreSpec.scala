package store

import java.time.Duration
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.scalatest.words.ShouldVerb



/**
  * Tests for the Store class.
  */
class StoreSpec extends TestKit(ActorSystem("CoordinatorSystem")) with ImplicitSender
  with ShouldVerb with WordSpecLike with Matchers with BeforeAndAfterAll {

  import Store._

  "Store#AddRequest" should {
    val store = system.actorOf(Store.props)

    "allow adding a request" in {
      store ! AddRequest(Array(), Array())
      expectMsgType[RequestAdded]
    }

    "return different ids when adding two requests" in {
      store ! AddRequest(Array(), Array())
      store ! AddRequest(Array(), Array())

      val Seq(response1, response2) = receiveN(2)

      response1 should not equal response2
    }
  }

  "Store#ListRequests" when {
    "empty" should {
      "return an empty request list" in {
        val store = system.actorOf(Store.props)

        store ! ListRequests
        expectMsg(RequestList(Seq()))
      }
    }

    "a request is added" should {
      "return that request's id" in {
        val store = system.actorOf(Store.props)

        store ! AddRequest(Array(), Array())
        val id = expectMsgType[RequestAdded].id

        store ! ListRequests
        val requests = expectMsgType[RequestList].ids

        requests should contain(id)
      }
    }

    "many requests are added" should {
      "return all those requests ids" in {
        val store = system.actorOf(Store.props)

        store ! AddRequest(Array(), Array())
        store ! AddRequest(Array(), Array())
        store ! AddRequest(Array(), Array())

        val ids = receiveN(3).map {
          case RequestAdded(id) => id
        }.toSet

        store ! ListRequests

        val requests = expectMsgType[RequestList].ids.toSet

        requests should equal(ids)
      }
    }
  }

  "Store#GetRequest" should {
    val store = system.actorOf(Store.props)

    "return a request with matching ID" in {
      val (expectedFrom, expectedTo) = (Array(0.toByte), Array(1.toByte))
      store ! AddRequest(expectedFrom, expectedTo)

      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)

      val RequestData(_, actualFrom, actualTo) = expectMsgType[RequestData]
      actualFrom should equal(expectedFrom)
      actualTo should equal(expectedTo)
    }

    "return NotFound when there is no matching id" in {
      val id = UUID.randomUUID()
      store ! ClaimRequest(id)
      expectMsg(NotFound(id))
    }

    "remove the request from the request list" in {
      val (expectedFrom, expectedTo) = (Array(0.toByte), Array(1.toByte))
      store ! AddRequest(expectedFrom, expectedTo)

      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! ListRequests
      expectMsgType[RequestList].ids should not contain(id)
    }
  }

  "Store#AddResponse" should {
    val store = system.actorOf(Store.props)

    "return NotFound when there is no matching request" in {
      val id = UUID.randomUUID()
      store ! AddResponse(id, Array())

      expectMsg(NotFound(id))
    }

    "return ResponseAdded when there is a matching request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))
    }

    "return ResponseAdded when there is a matching pending request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))
    }

    "remove the corresponding request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! AddResponse(id, Array())

      expectMsgType[ResponseAdded]

      store ! ListRequests

      val requests = expectMsgType[RequestList].ids
      requests should not contain id
    }

    "return ResponseRepeated when a response is added twice for the same ID" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))

      store ! AddResponse(id, Array())
      expectMsg(ResponseRepeated(id))
    }
  }

  "Store#GetResponse" should {
    val store = system.actorOf(Store.props)

    "return NotFound for an ID that matches neither a response nor a request" in {
      val id = UUID.randomUUID()
      store ! GetResponse(id)
      expectMsg(NotFound(id))
    }

    "return NotCompleted for an ID that matches a request but not a response" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! GetResponse(id)

      expectMsg(NotCompleted(id))
    }

    "return NotCompleted for an ID that matches a pending request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! GetResponse(id)
      expectMsg(NotCompleted(id))

    }

    "return ResponseData for an ID that matches a response" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      val expected = Array(0.toByte)
      store ! AddResponse(id, expected)

      expectMsg(ResponseAdded(id))

      store ! GetResponse(id)

      val data = expectMsgType[ResponseData].data

      data should equal(expected)
    }
  }

  "Store#Cleanup" should {
    val store = system.actorOf(Store.props)
    "remove an old request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      Thread.sleep(1)

      store ! Cleanup(Duration.ofNanos(1))
      expectMsg(CleanupCompleted)

      store ! ClaimRequest(id)

      expectMsg(NotFound(id))
    }

    "remove an old response" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))

      Thread.sleep(1)

      store ! Cleanup(Duration.ofNanos(1))
      expectMsg(CleanupCompleted)

      store ! GetResponse(id)

      expectMsg(NotFound(id))
    }

    "not remove a not old request" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! Cleanup(Duration.ofDays(1))
      expectMsg(CleanupCompleted)

      store ! ClaimRequest(id)
      expectMsgType[RequestData].id should equal(id)
    }

    "not remove a not old response" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))

      store ! Cleanup(Duration.ofDays(1))
      expectMsg(CleanupCompleted)

      store ! GetResponse(id)

      expectMsgType[ResponseData].id should equal(id)
    }

    "move an un-responded-to pending request back to the request list" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! Cleanup(Duration.ofDays(1), Duration.ofDays(0), Duration.ofDays(1))
      expectMsg(CleanupCompleted)

      store ! ListRequests
      expectMsgType[RequestList].ids should contain(id)
    }

    "not move a responded-to request from the pending list to the request list" in {
      store ! AddRequest(Array(), Array())
      val id = expectMsgType[RequestAdded].id

      store ! ClaimRequest(id)
      expectMsgType[RequestData]

      store ! AddResponse(id, Array())
      expectMsg(ResponseAdded(id))

      store ! Cleanup(Duration.ofDays(1), Duration.ofDays(0), Duration.ofDays(1))
      expectMsg(CleanupCompleted)

      store ! ListRequests
      expectMsgType[RequestList].ids should not contain(id)
    }
  }


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
