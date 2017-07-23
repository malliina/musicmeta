package controllers

import com.malliina.musicmeta.{MusicTags, UserFeedback}
import com.malliina.play.ActorExecution
import com.malliina.play.controllers.{AuthBundle, BaseSecurity}
import com.malliina.play.models.AuthRequest
import play.api.Logger
import play.api.mvc.Results.{Ok, Redirect}

object MetaOAuth {
  private val log = Logger(getClass)

  def forOAuth(oauth: MetaOAuthControl, ctx: ActorExecution) = {
    val authBundle = AuthBundle.forOAuth(oauth)
    new MetaOAuth(oauth, authBundle, ctx)
  }
}

class MetaOAuth(val oauth: MetaOAuthControl,
                val auth: AuthBundle[AuthRequest],
                ctx: ActorExecution)
  extends BaseSecurity(oauth.actions, auth, oauth.mat) {

  val streamer = new LogStreamer(auth.authenticator, ctx)

  def index = authAction(_ => Ok(MusicTags.index))

  def logs = authAction(_ => Ok(MusicTags.logs(None)))

  def openSocket = streamer.sockets.newSocket

  def eject = logged {
    oauth.actions { req =>
      val feedback = UserFeedback.flashed(req.flash, oauth.messageKey)
      Ok(MusicTags.eject(feedback))
    }
  }

  def logout = authAction { _ =>
    Redirect(routes.MetaOAuth.eject()).withNewSession
      .flashing(oauth.messageKey -> oauth.logoutMessage)
  }
}
