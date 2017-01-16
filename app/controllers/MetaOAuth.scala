package controllers

import com.malliina.play.controllers.OAuthSecured
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, WebSocket}
import views.html

object MetaOAuth {
  val MessageKey = "message"
}

class MetaOAuth(oauth: MetaOAuthControl) extends OAuthSecured(oauth, oauth.mat) {
  val streamer = new LogStreamer(req => authenticate(req).map(_.get), mat)

  def openSocket: WebSocket = streamer.openSocket

  def index = authAction(_ => Ok(views.html.index()))

  def logs = authAction(req => Ok(views.html.logs(None, streamer, req)))

  def eject = logged(Action(req => Ok(html.eject(req.flash))))

  def logout = authAction(req => {
    Redirect(routes.MetaOAuth.eject()).withNewSession
      .flashing(oauth.messageKey -> oauth.logoutMessage)
  })
}
