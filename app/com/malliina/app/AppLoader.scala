package com.malliina.app

import controllers.{MetaOAuth, Assets, Covers}
import play.api.routing.Router
import play.api.{Logger, BuiltInComponentsFromContext, Application, ApplicationLoader}
import play.api.ApplicationLoader.Context
import router.Routes

/**
  * @author mle
  */
class AppLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    Logger.configure(context.environment)
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  lazy val assets = new Assets(httpErrorHandler)
  lazy val oauth = new MetaOAuth
  lazy val covers = new Covers(actorSystem, oauth)
  override val router: Router = new Routes(httpErrorHandler, oauth, covers, assets)
}
