package controllers

import com.mle.oauth.OAuthControl
import play.api.mvc.{Call, Result}

/**
 * @author Michael
 */
trait MetaOAuth extends OAuthControl {
  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def oAuthRedir: Call = routes.Home.redirResponse()

  override def onOAuthSuccess: Call = routes.Home.logs()

  override def onOAuthUnauthorized(email: String): Result = ejectWith(unauthorizedMessage(email))

  private def ejectWith(message: String) = Redirect(routes.Home.eject()).flashing(
    "message" -> message
  )
}
