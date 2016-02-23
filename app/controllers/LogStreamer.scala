package controllers

import com.malliina.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.malliina.maps.{ItemMap, StmItemMap}
import com.malliina.play.controllers.{AuthResult, LogStreaming}
import com.malliina.play.ws.WebSocketClient
import play.api.mvc.{Call, RequestHeader}
import rx.lang.scala.Subscription

import scala.concurrent.Future

class LogStreamer(auth: RequestHeader => Future[AuthResult]) extends LogStreaming {
  val appender = LogbackUtils.appender[BasicBoundedReplayRxAppender]("RX")
    .getOrElse(new BasicBoundedReplayRxAppender)
  val subscriptions: ItemMap[WebSocketClient, Subscription] =
    StmItemMap.empty[WebSocketClient, Subscription]

  override def authenticateAsync(req: RequestHeader): Future[AuthResult] = auth(req)

  override def openSocketCall: Call = routes.MetaOAuth.openSocket()
}
