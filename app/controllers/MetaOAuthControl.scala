package controllers

import akka.stream.Materializer
import com.malliina.oauth.GoogleOAuthCredentials
import com.malliina.play.controllers.OAuthControl
import play.api.mvc.{ActionBuilder, AnyContent, Call, Request}

class MetaOAuthControl(val actions: ActionBuilder[Request, AnyContent], creds: GoogleOAuthCredentials, mat: Materializer)
  extends OAuthControl(actions, creds, mat) {

  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def startOAuth: Call = routes.MetaOAuthControl.initiate()

  override def oAuthRedir: Call = routes.MetaOAuthControl.redirResponse()

  override def onOAuthSuccess: Call = routes.MetaOAuth.logs()

  override def ejectCall: Call = routes.MetaOAuth.eject()
}
