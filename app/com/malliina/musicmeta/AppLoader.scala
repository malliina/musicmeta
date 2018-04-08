package com.malliina.musicmeta

import com.malliina.oauth.{DiscoGsOAuthCredentials, DiscoGsOAuthReader, GoogleOAuthCredentials, GoogleOAuthReader}
import com.malliina.play.ActorExecution
import com.malliina.play.app.DefaultApp
import controllers._
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import play.filters.headers.SecurityHeadersConfig
import play.filters.hosts.AllowedHostsConfig
import router.Routes

import scala.concurrent.Future

class AppLoader extends DefaultApp(AppComponents.prod)

object AppComponents {
  def prod(ctx: Context) = new AppComponents(ctx, DiscoGsOAuthReader.load, GoogleOAuthReader.load)
}

class AppComponents(context: Context, creds: DiscoGsOAuthCredentials, google: GoogleOAuthCredentials)
  extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with AssetsComponents {

  val allowedCsp = Seq(
    "*.musicpimp.org",
    "*.bootstrapcdn.com",
    "*.googleapis.com",
    "code.jquery.com",
    "use.fontawesome.com",
    "cdnjs.cloudflare.com"
  )
  val allowedEntry = allowedCsp.mkString(" ")

  val csp = s"default-src 'self' 'unsafe-inline'  $allowedEntry; connect-src *; img-src 'self' data:;"
  override lazy val securityHeadersConfig = SecurityHeadersConfig(contentSecurityPolicy = Option(csp))
  override lazy val allowedHostsConfig = AllowedHostsConfig(Seq("localhost", "api.musicpimp.org"))

  lazy val oauthControl = new MetaOAuthControl(controllerComponents.actionBuilder, google)
  lazy val exec = ActorExecution(actorSystem, materializer)
  lazy val oauth = MetaOAuth("username", MetaHtml(environment.mode), defaultActionBuilder, exec)
  lazy val covers = new Covers(oauth, creds, controllerComponents)
  lazy val metaAssets = new MetaAssets(assets)
  override val router: Router = new Routes(httpErrorHandler, oauth, oauthControl, covers, metaAssets)

  applicationLifecycle.addStopHook { () =>
    Future.successful(oauthControl.http.close())
  }
}
