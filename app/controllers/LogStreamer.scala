package controllers

import akka.actor.Props
import com.malliina.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.malliina.logstreams.client.LogEvents
import com.malliina.play.ActorExecution
import com.malliina.play.auth.{Authenticator, UserAuthenticator}
import com.malliina.play.models.Username
import com.malliina.play.ws.{ActorConfig, ObserverActor, Sockets}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Call
import rx.lang.scala.Observable

import scala.concurrent.duration.DurationInt

object LogStreamer {
  def sockets(events: Observable[JsValue],
              auth: Authenticator[Username],
              ctx: ActorExecution): Sockets[Username] = {
    new Sockets(auth, ctx) {
      override def props(conf: ActorConfig[Username]) =
        Props(new ObserverActor(events, conf))
    }
  }

  def apply(ctx: ActorExecution): LogStreamer =
    new LogStreamer(UserAuthenticator.session(), ctx)
}

class LogStreamer(auth: Authenticator[Username],
                  ctx: ActorExecution) {
  lazy val jsonEvents = LogbackUtils.getAppender[BasicBoundedReplayRxAppender]("RX")
    .logEvents
    .tumblingBuffer(50.millis)
    .filter(_.nonEmpty)
    .map(es => Json.toJson(LogEvents(es)))
  lazy val sockets = LogStreamer.sockets(jsonEvents, UserAuthenticator.session(), ctx)

  def openSocketCall: Call = routes.MetaOAuth.openSocket()
}
