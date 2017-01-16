package tests

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.malliina.app.AppLoader
import controllers.{Covers, MetaOAuth, MetaOAuthControl}
import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplicationLoader}
import play.api.{Application, http}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class WithApp extends WithApplicationLoader(new AppLoader)

class APITests extends Specification with Results {
  implicit val timeout = 20.seconds
  implicit val actorSystem = ActorSystem("test")
  implicit val mat = ActorMaterializer()
  val oauthControl = new MetaOAuthControl(mat, isProd = false)
  val oauth = new MetaOAuth(oauthControl)
  val covers = new Covers(actorSystem, oauth, mat)

  "App" should {
    "respond to ping" in {
      verifyActionResponse(covers.ping, OK)
    }

    "proper cover search" in {
      verifyActionResponse(covers.cover, OK, FakeRequest(GET, "/covers?artist=iron%20maiden&album=powerslave"))
    }

    "nonexistent cover return 404" in {
      verifyActionResponse(covers.cover, NOT_FOUND, FakeRequest(GET, "/covers?artist=zyz&album=abcde"))
    }

    "invalid request returns HTTP 400 BAD REQUEST" in {
      verifyActionResponse(covers.cover, http.Status.BAD_REQUEST)
    }

    "router.ping" in new WithApp {
      val result = getRequest(app, "/ping")
      status(result) must equalTo(OK)
    }

    "request to nonexistent URL returns 404" in new WithApp {
      val result = getRequest(app, "/ping2")
      status(result) mustEqual NOT_FOUND
    }

    "router.badrequest" in new WithApp {
      val result = getRequest(app, "/covers?artist=abba&album=")
      status(result) mustEqual BAD_REQUEST
    }
  }

  private def getRequest(app: Application, path: String) =
    route(app, FakeRequest(GET, path)).get

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
