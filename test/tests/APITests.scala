package tests

import akka.actor.ActorSystem
import com.malliina.app.AppLoader
import controllers.{Covers, MetaOAuth}
import org.specs2.mutable.Specification
import play.api.http
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplicationLoader}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class WithApp extends WithApplicationLoader(new AppLoader)

class APITests extends Specification with Results {
  implicit val timeout = 20.seconds
  val actorSystem = ActorSystem("test")
  val oauth = new MetaOAuth
  val covers = new Covers(actorSystem, oauth)

  "App" should {
    "respond to ping" in {
      verifyActionResponse(covers.ping, http.Status.OK)
    }

    "proper cover search" in {
      verifyActionResponse(covers.cover, http.Status.OK, FakeRequest(GET, "/covers?artist=iron%20maiden&album=powerslave"))
    }

    "nonexistent cover return 404" in {
      verifyActionResponse(covers.cover, http.Status.NOT_FOUND, FakeRequest(GET, "/covers?artist=zyz&album=abcde"))
    }

    "invalid request returns HTTP 400 BAD REQUEST" in {
      verifyActionResponse(covers.cover, http.Status.BAD_REQUEST)
    }

    "router.ping" in new WithApp {
      val result = getRequest("/ping")
      status(result) must equalTo(OK)
    }

    "request to nonexistent URL returns 404" in new WithApp {
      val result = getRequest("/ping2")
      status(result) mustEqual NOT_FOUND
    }

    "router.badrequest" in new WithApp {
      val result = getRequest("/covers?artist=abba&album=")
      status(result) mustEqual BAD_REQUEST
    }
  }

  private def getRequest(path: String) = route(FakeRequest(GET, path)).get

  private def verifyActionResponse(action: EssentialAction,
                                   expectedStatus: Int,
                                   req: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()) = {
    verifyResponse(action.apply(req).run, expectedStatus)
  }

  private def verifyResponse(result: Future[Result], expectedStatus: Int = http.Status.OK) = {
    val statusCode = Await.result(result, timeout).header.status
    statusCode mustEqual expectedStatus
  }
}
