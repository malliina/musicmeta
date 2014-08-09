package controllers

import java.nio.file.Paths

import com.mle.http.{DiscoClient, Info}
import com.mle.logbackrx.{BasicBoundedReplayRxAppender, LogEvent, LogbackUtils}
import com.mle.musicmeta.Starter
import com.mle.play.actions.Actions.SyncAction
import com.mle.play.concurrent.ExecutionContexts.synchronousIO
import com.mle.play.controllers.BaseSecurity
import com.mle.util.FileImplicits.StorageFile
import com.mle.util.{FileUtilities, Log}
import play.api.mvc._
import rx.lang.scala.Observable
import views.html

import scala.concurrent.Future

/**
 *
 * @author mle
 */
object Home extends Controller with BaseSecurity with StreamingLogController with MetaOAuth with Log {
  val fallbackCoverDir = FileUtilities.tempDir / "covers"
  val coverDir = sys.props.get("cover.dir").fold(fallbackCoverDir)(path => Paths.get(path))
  val covers = new DiscoClient(Info.discoGsAuthReader.load, coverDir)
  lazy val appender = LogbackUtils.getAppender[BasicBoundedReplayRxAppender]("RX")

  override def logEvents: Observable[LogEvent] = appender.logEvents

  def index = AuthAction(req => Ok(views.html.index()))

  def ping = Logged(Action(Ok))

  def cover = Logged(SyncAction.async(request => {
    def message(msg: String) = s"From ${request.remoteAddress}: $msg"
    def query(key: String) = (request getQueryString key).filter(_.nonEmpty)
    (for {
      artist <- query("artist")
      album <- query("album")
    } yield {
      covers.cover(artist, album).map(path => {
        log info message(s"Serving cover: $artist - $album")
        Ok.sendFile(path.toFile)
      }).recover {
        case nse: NoSuchElementException =>
          log info message(s"Unable to find cover: $artist - $album")
          NotFound
        case t: Throwable =>
          log.error(message(s"Failure while searching cover: $artist - $album"), t)
          InternalServerError
      }
    }).getOrElse(Future successful BadRequest)
  }))

  def logs = AuthAction(implicit request => Ok(views.html.logs()))

  def eject = Logged(Action(implicit request => Ok(html.eject())))

  def logout = AuthAction(implicit request => {
    Redirect(routes.Home.eject()).withNewSession.flashing(
      "message" -> "You have successfully signed out."
    )
  })

  override protected def onUnauthorized(implicit req: RequestHeader): Result = Redirect(routes.Home.initiate())

  override def validateCredentials(user: String, pass: String): Boolean = false

  override def wsUrl(implicit request: RequestHeader): String = routes.Home.openLogSocket().webSocketURL(Starter.isHttpsAvailable)
}
