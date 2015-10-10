package controllers

import java.net.ConnectException
import java.nio.file.Paths

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.file.{FileUtilities, StorageFile}
import com.mle.http.{ResponseException, CoverNotFoundException, DiscoClient}
import com.mle.oauth.DiscoGsOAuthReader
import com.mle.play.actions.Actions.SyncAction
import com.mle.util.Log
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
 * @author Michael
 */
object Covers extends Controller with Log {
  val fallbackCoverDir = FileUtilities.tempDir / "covers"
  val coverDir = sys.props.get("cover.dir").fold(fallbackCoverDir)(path => Paths.get(path))
  val covers = new DiscoClient(DiscoGsOAuthReader.load, coverDir)

  def ping = MetaOAuth.Logged(Action(Ok))

  def cover = MetaOAuth.Logged(SyncAction.async(request => {
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
          case cnfe: CoverNotFoundException =>
            log info message(s"Unable to find cover: $coverName")
            NotFound
          case nse: NoSuchElementException =>
            log info message(s"Unable to find cover: $coverName")
            NotFound
          case re: ResponseException =>
            log.error(s"Invalid response recieved", re)
            BadGateway
          case ce: ConnectException =>
            log.warn(message(s"Unable to search for cover: $coverName. Unable to connect to cover backend: ${ce.getMessage}"), ce)
            BadGateway
          case t: Throwable =>
            log.error(message(s"Failure while searching cover: $coverName"), t)
            InternalServerError
        }
      }).getOrElse(Future successful BadRequest)
  }))
}
