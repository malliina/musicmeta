package controllers

import com.malliina.musicmeta.{MusicTags, UserFeedback}
import com.malliina.play.ActorExecution
import com.malliina.play.controllers.{AuthBundle, BaseSecurity}
import com.malliina.play.models.Username
import play.api.mvc.Action
import play.api.mvc.Results.{Ok, Redirect}

object MetaOAuth {
  def forOAuth(oauth: MetaOAuthControl, ctx: ActorExecution) = {
    val authBundle = AuthBundle.oauth(oauth.startOAuth, oauth.sessionUserKey)
    new MetaOAuth(oauth, authBundle, ctx)
  }
}

class MetaOAuth(val oauth: MetaOAuthControl,
                val auth: AuthBundle[Username],
                ctx: ActorExecution)
  extends BaseSecurity(auth, oauth.mat) {

  val streamer = new LogStreamer(auth.authenticator, ctx)

  def index = authAction(_ => Ok(MusicTags.index))

  def logs = authAction(_ => Ok(MusicTags.logs(None)))

  def openSocket = streamer.sockets.newSocket

  def eject = logged {
    Action { req =>
      val feedback = UserFeedback.flashed(req.flash, oauth.messageKey)
      Ok(MusicTags.eject(feedback))
    }
  }

  def logout = authAction { _ =>
    Redirect(routes.MetaOAuth.eject()).withNewSession
      .flashing(oauth.messageKey -> oauth.logoutMessage)
  }
}
