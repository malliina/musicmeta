package controllers

import com.mle.play.controllers.OAuthSecured
import play.api.mvc.{Action, Call}
import views.html

/**
 * @author Michael
 */
object MetaOAuth extends OAuthSecured {
  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def startOAuth: Call = routes.MetaOAuth.initiate()

  override def oAuthRedir: Call = routes.MetaOAuth.redirResponse()

  override def onOAuthSuccess: Call = routes.MetaOAuth.logs()

  override def ejectCall: Call = routes.MetaOAuth.eject()

  def index = AuthAction(_ => Ok(views.html.index()))

  def logs = AuthAction(implicit request => Ok(views.html.logs()))

  def eject = Logged(Action(implicit request => Ok(html.eject())))

  def logout = AuthAction(implicit request => {
    Redirect(routes.MetaOAuth.eject()).withNewSession.flashing(messageKey -> logoutMessage)
  })
}
