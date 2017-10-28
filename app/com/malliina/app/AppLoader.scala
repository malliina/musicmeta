package com.malliina.app

import com.malliina.oauth.{DiscoGsOAuthCredentials, DiscoGsOAuthReader, GoogleOAuthCredentials, GoogleOAuthReader}
import com.malliina.play.ActorExecution
import com.malliina.play.app.DefaultApp
import controllers.{AssetsComponents, Covers, MetaOAuth, MetaOAuthControl}
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.headers.SecurityHeadersConfig
import router.Routes

class AppLoader extends DefaultApp(AppComponents.prod)

object AppComponents {
  def prod(ctx: Context) = new AppComponents(ctx, DiscoGsOAuthReader.load, GoogleOAuthReader.load)
}

class AppComponents(context: Context, creds: DiscoGsOAuthCredentials, google: GoogleOAuthCredentials)
  extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {

  val csp = "default-src 'self' 'unsafe-inline' *.musicpimp.org *.bootstrapcdn.com *.googleapis.com; connect-src *"
  override lazy val securityHeadersConfig = SecurityHeadersConfig(contentSecurityPolicy = Option(csp))
  override def httpFilters: Seq[EssentialFilter] = Seq(csrfFilter, securityHeadersFilter)
  lazy val oauthControl = new MetaOAuthControl(controllerComponents.actionBuilder, google)
  lazy val exec = ActorExecution(actorSystem, materializer)
  lazy val oauth = MetaOAuth.forOAuth(oauthControl, exec)
  lazy val covers = new Covers(oauth, creds, controllerComponents)
  override val router: Router = new Routes(httpErrorHandler, oauth, oauthControl, covers, assets)
}
