package controllers

import java.net.ConnectException
import java.nio.file.Paths

import com.mle.file.{FileUtilities, StorageFile}
import com.mle.http.DiscoClient
import com.mle.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.mle.oauth.DiscoGsOAuthReader
import com.mle.play.actions.Actions.SyncAction
import com.mle.play.concurrent.ExecutionContexts.synchronousIO
import com.mle.play.controllers.{BaseSecurity, LogStreaming}
import com.mle.play.ws.SyncAuth
import com.mle.util.Log
import play.api.mvc._
import views.html

import scala.concurrent.Future

/**
 *
 * @author mle
 */
object Home extends Controller with BaseSecurity with LogStreaming with MetaOAuth with SyncAuth with Log {
  val fallbackCoverDir = FileUtilities.tempDir / "covers"
  val coverDir = sys.props.get("cover.dir").fold(fallbackCoverDir)(path => Paths.get(path))
  val covers = new DiscoClient(DiscoGsOAuthReader.load, coverDir)
  lazy val appender = LogbackUtils.getAppender[BasicBoundedReplayRxAppender]("RX")

  override def openSocketCall: Call = routes.Home.openSocket()

  override def startOAuth: Call = routes.Home.initiate()

  override def ejectCall: Call = routes.Home.eject()

  def index = AuthAction(req => Ok(views.html.index()))

  def ping = Logged(Action(Ok))

  def cover = Logged(SyncAction.async(request => {
    def message(msg: String) = s"From ${request.remoteAddress}: $msg"
    def query(key: String) = (request getQueryString key).filter(_.nonEmpty)
    (for {
      artist <- query("artist")
      album <- query("album")
    } yield {
      val coverName = s"$artist - $album"
      covers.cover(artist, album).map(path => {
        log info message(s"Serving cover: $coverName")
        Ok.sendFile(path.toFile)
      }).recover {
        case nse: NoSuchElementException =>
          log info message(s"Unable to find cover: $coverName")
          NotFound
        case ce: ConnectException =>
          log.warn(message(s"Unable to search for cover: $coverName. Unable to connect to cover backend: ${ce.getMessage}"), ce)
          BadGateway
        case t: Throwable =>
          log.error(message(s"Failure while searching cover: $coverName"), t)
          InternalServerError
      }
    }).getOrElse(Future successful BadRequest)
  }))

  def logs = AuthAction(implicit request => Ok(views.html.logs()))

  def eject = Logged(Action(implicit request => Ok(html.eject())))

  def logout = AuthAction(implicit request => {
    Redirect(routes.Home.eject()).withNewSession.flashing(messageKey -> logoutMessage)
  })
}
