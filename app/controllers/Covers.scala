package controllers

import java.net.ConnectException
import java.nio.file.Paths

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.file.{FileUtilities, StorageFile}
import com.malliina.http.{CoverNotFoundException, DiscoClient, ResponseException}
import com.malliina.oauth.DiscoGsOAuthCredentials
import com.malliina.play.ActorExecution
import com.malliina.play.actions.Actions.SyncAction
import com.malliina.play.http.Proxies
import controllers.Covers.log
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

object Covers {
  private val log = Logger(getClass)
}

class Covers(oauth: MetaOAuth,
             creds: DiscoGsOAuthCredentials,
             ctx: ActorExecution)
  extends Controller {

  val syncAction = new SyncAction(ctx.actorSystem)
  val fallbackCoverDir = FileUtilities.tempDir / "covers"
  val coverDir = sys.props.get("cover.dir").fold(fallbackCoverDir)(path => Paths.get(path))
  val covers = new DiscoClient(creds, coverDir, ctx.materializer)

  def ping = oauth.logged(Action(Ok))

  def cover = oauth.logged {
    syncAction.async { request =>
      def message(msg: String) = s"From '${Proxies.realAddress(request)}': $msg"

      def query(key: String) = (request getQueryString key).filter(_.nonEmpty)

      val result = for {
        artist <- query("artist")
        album <- query("album")
      } yield {
        val coverName = s"$artist - $album"
        covers.cover(artist, album).map(path => {
          log info message(s"Serving cover '$coverName' at '$path'.")
          Ok.sendFile(path.toFile)
        }).recover {
          case cnfe: CoverNotFoundException =>
            log info message(s"Unable to find cover '$coverName'.")
            NotFound
          case nse: NoSuchElementException =>
            log info message(s"Unable to find cover '$coverName'.")
            NotFound
          case re: ResponseException =>
            log.error(s"Invalid response received.", re)
            BadGateway
          case ce: ConnectException =>
            log.warn(message(s"Unable to search for cover '$coverName'. Unable to connect to cover backend: ${ce.getMessage}"), ce)
            BadGateway
          case t: Throwable =>
            log.error(message(s"Failure while searching cover '$coverName'."), t)
            InternalServerError
        }
      }
      result getOrElse Future.successful(BadRequest)
    }
  }
}
