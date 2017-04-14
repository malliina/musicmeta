package controllers

import com.malliina.musicmeta.{MusicTags, UserFeedback}
import com.malliina.play.controllers.OAuthSecured
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, WebSocket}

object MetaOAuth {
  val MessageKey = "message"
}

class MetaOAuth(oauth: MetaOAuthControl)
  extends OAuthSecured(oauth, oauth.mat) {
  val streamer = new LogStreamer(req => authenticate(req).map(_.get), mat, oauth.isProd)

  def openSocket: WebSocket = streamer.openSocket

  def index = authAction(_ => Ok(MusicTags.index))

  def logs = authAction(_ => Ok(MusicTags.logs(None)))

  def eject = logged {
    Action { req =>
      val feedback = UserFeedback.flashed(req.flash, oauth.messageKey)
      Ok(MusicTags.eject(feedback))
    }
  }

  def logout = authAction(req => {
    Redirect(routes.MetaOAuth.eject()).withNewSession
      .flashing(oauth.messageKey -> oauth.logoutMessage)
  })
}
