package controllers

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.musicmeta.{BuildMeta, MetaHtml, UserFeedback}
import com.malliina.play.ActorExecution
import com.malliina.play.controllers.{AuthBundle, BaseSecurity}
import com.malliina.play.http.AuthedRequest
import com.malliina.play.models.AuthRequest
import play.api.libs.json.Json
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{ActionBuilder, AnyContent, Request}

object MetaOAuth {
  def apply(sessionKey: String, actions: ActionBuilder[Request, AnyContent], ctx: ActorExecution) = {
    val bundle = AuthBundle.oauth((r, u) => AuthedRequest(u, r), routes.MetaOAuthControl.googleStart(), sessionKey)
    new MetaOAuth(actions, bundle, ctx)
  }
}

class MetaOAuth(actions: ActionBuilder[Request, AnyContent],
                auth: AuthBundle[AuthRequest],
                ctx: ActorExecution)
  extends BaseSecurity(actions, auth, ctx.materializer) {

  val streamer = new LogStreamer(auth.authenticator, ctx)
  val messageKey = "message"

  def index = authAction(_ => Ok(MetaHtml.index))

  def health = actions(Ok(Json.toJson(BuildMeta.default)))

  def logs = authAction(_ => Ok(MetaHtml.logs(None)))

  def openSocket = streamer.sockets.newSocket

  def eject = logged {
    actions { req =>
      val feedback = UserFeedback.flashed(req.flash, messageKey)
      Ok(MetaHtml.eject(feedback))
    }
  }

  def logout = authAction { _ =>
    Redirect(routes.MetaOAuth.eject()).withNewSession
      .flashing(messageKey -> "Logged out.")
  }
}
