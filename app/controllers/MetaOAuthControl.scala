package controllers

import com.malliina.oauth.GoogleOAuthCredentials
import com.malliina.play.controllers.OAuthControl
import com.malliina.play.models.Email
import play.api.mvc.{ActionBuilder, AnyContent, Call, Request}

class MetaOAuthControl(actions: ActionBuilder[Request, AnyContent], creds: GoogleOAuthCredentials)
  extends OAuthControl(actions, creds) {

  override def isAuthorized(email: Email): Boolean = email == Email("malliina123@gmail.com")

  override def startOAuth: Call = routes.MetaOAuthControl.initiate()

  override def oAuthRedir: Call = routes.MetaOAuthControl.redirResponse()

  override def onOAuthSuccess: Call = routes.MetaOAuth.logs()

  override def ejectCall: Call = routes.MetaOAuth.eject()
}
