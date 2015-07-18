package tests

import controllers.{Covers, MetaOAuth, Sockets}
import org.scalatest.FunSuite
import org.scalatestplus.play.OneServerPerSuite
import play.api.http
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * @author Michael
 */
class APITests extends FunSuite with OneServerPerSuite with Results {
  implicit val timeout = 5.seconds

  test("responds to ping") {
    verifyActionResponse(Covers.ping, http.Status.OK)
  }
  test("router.ping") {
    val Some(result) = route(FakeRequest(GET, "/ping"))
    verifyResponse(result, http.Status.OK)
  }
  test("request to nonexistent URL returns 404") {
    val Some(result) = route(FakeRequest(GET, "/ping2"))
    verifyResponse(result, http.Status.NOT_FOUND)
  }
  test("router.badrequest") {
    val Some(result) = route(FakeRequest(GET, "/covers?artist=abba&album="))
    verifyResponse(result, http.Status.BAD_REQUEST)
  }
  test("responds to proper cover search") {
    verifyActionResponse(Covers.cover, http.Status.OK, FakeRequest(GET, "/covers?artist=iron%20maiden&album=powerslave"))
  }
  test("nonexistent cover search returns HTTP 404 NOT FOUND") {
    verifyActionResponse(Covers.cover, http.Status.NOT_FOUND, FakeRequest(GET, "/covers?artist=zyz&album=abcde"))
  }
  test("invalid request returns HTTP 400 BAD REQUEST") {
    verifyActionResponse(Covers.cover, http.Status.BAD_REQUEST)
  }

  private def verifyActionResponse(action: EssentialAction, expectedStatus: Int, req: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()) = {
    verifyResponse(action.apply(req).run, expectedStatus)
  }

  private def verifyResponse(result: Future[Result], expectedStatus: Int = http.Status.OK) = {
    val statusCode = Await.result(result, timeout).header.status
    assert(statusCode === expectedStatus)
  }
}
