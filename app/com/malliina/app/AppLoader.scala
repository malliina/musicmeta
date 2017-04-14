package com.malliina.app

import com.malliina.oauth.{DiscoGsOAuthCredentials, DiscoGsOAuthReader}
import com.malliina.play.ActorExecution
import com.malliina.play.app.DefaultApp
import controllers.MetaOAuthControl
import controllers.{Assets, Covers, MetaOAuth}
import play.api.routing.Router
import play.api._
import play.api.ApplicationLoader.Context
import controllers._
import router.Routes

class AppLoader extends DefaultApp(AppComponents.prod)

object AppComponents {
  def prod(ctx: Context) = new AppComponents(ctx, DiscoGsOAuthReader.load)
}

class AppComponents(context: Context, creds: DiscoGsOAuthCredentials)
  extends BuiltInComponentsFromContext(context) {
  lazy val assets = new Assets(httpErrorHandler)
  lazy val oauthControl = new MetaOAuthControl(materializer)
  lazy val exec = ActorExecution(actorSystem, materializer)
  lazy val oauth = MetaOAuth.forOAuth(oauthControl, exec)
  lazy val covers = new Covers(oauth, creds, exec)
  override val router: Router = new Routes(httpErrorHandler, oauth, oauthControl, covers, assets)
}
