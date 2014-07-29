package controllers

import com.mle.play.openid.OpenIdAuth
import play.api.libs.openid.UserInfo
import play.api.mvc.{Call, Controller, Result}

/**
 * @author Michael
 */
trait MetaOpenID extends Controller with OpenIdAuth {
  override def successRedirect: Call = routes.Home.logs()

  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def onOpenIdFailure: Result = ejectWith("Something went wrong!")

  override def onOpenIdUnauthorized(user: UserInfo) = ejectWith(s"Hi ${userString(user)}, you're not authorized.")

  private def ejectWith(message: String) = Redirect(routes.Home.eject()).flashing(
    "message" -> message
  )
}
