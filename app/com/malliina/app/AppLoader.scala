package com.malliina.app

import com.malliina.play.app.DefaultApp
import controllers.MetaOAuthControl
import controllers.{Assets, Covers, MetaOAuth}
import play.api.routing.Router
import play.api._
import play.api.ApplicationLoader.Context
import router.Routes

class AppLoader extends DefaultApp(new AppComponents(_))

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  lazy val assets = new Assets(httpErrorHandler)
  lazy val oauthControl = new MetaOAuthControl(materializer, environment.mode == Mode.Prod)
  lazy val oauth = new MetaOAuth(oauthControl)
  lazy val covers = new Covers(actorSystem, oauth, materializer)
  override val router: Router = new Routes(httpErrorHandler, oauth, oauthControl, covers, assets)
}
