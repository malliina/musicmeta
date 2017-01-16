package controllers

import akka.stream.Materializer
import com.malliina.play.controllers.OAuthControl
import play.api.mvc.{Call, RequestHeader}

class MetaOAuthControl(val mat: Materializer, isProd: Boolean) extends OAuthControl(mat) {
  // temp hack
  override def redirURL(request: RequestHeader): String =
    oAuthRedir.absoluteURL(secure = isProd)(request)

  override def isAuthorized(email: String): Boolean = email == "malliina123@gmail.com"

  override def startOAuth: Call = routes.MetaOAuthControl.initiate()

  override def oAuthRedir: Call = routes.MetaOAuthControl.redirResponse()

  override def onOAuthSuccess: Call = routes.MetaOAuth.logs()

  override def ejectCall: Call = routes.MetaOAuth.eject()
}
