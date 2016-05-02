package controllers

import akka.stream.Materializer
import com.malliina.play.controllers.OAuthSecured
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Call, WebSocket}
import views.html

object MetaOAuth {
  val MessageKey = "message"
}

class MetaOAuth(val mat: Materializer) extends OAuthSecured {
  val streamer = new LogStreamer(req => authenticate(req).map(_.get), mat)

  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def startOAuth: Call = routes.MetaOAuth.initiate()

  override def oAuthRedir: Call = routes.MetaOAuth.redirResponse()

  override def onOAuthSuccess: Call = routes.MetaOAuth.logs()

  override def ejectCall: Call = routes.MetaOAuth.eject()

  def openSocket: WebSocket = streamer.openSocket

  def index = AuthAction(_ => Ok(views.html.index()))

  def logs = AuthAction(implicit request => Ok(views.html.logs(None, streamer)))

  def eject = Logged(Action(implicit request => Ok(html.eject())))

  def logout = AuthAction(implicit request => {
    Redirect(routes.MetaOAuth.eject()).withNewSession.flashing(messageKey -> logoutMessage)
  })
}
