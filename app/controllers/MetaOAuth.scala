package controllers

import com.mle.play.controllers.OAuthSecured
import play.api.mvc.Call

/**
 * @author Michael
 */
trait MetaOAuth extends OAuthSecured {
  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def oAuthRedir: Call = routes.Home.redirResponse()

  override def onOAuthSuccess: Call = routes.Home.logs()
}
