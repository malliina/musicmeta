package controllers

import akka.stream.Materializer
import com.malliina.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.malliina.maps.{ItemMap, StmItemMap}
import com.malliina.play.controllers.LogStreaming
import com.malliina.play.http.AuthResult
import com.malliina.play.ws.WebSocketClient
import play.api.mvc.{Call, RequestHeader}
import rx.lang.scala.Subscription

import scala.concurrent.Future

class LogStreamer(auth: RequestHeader => Future[AuthResult], val mat: Materializer)
  extends LogStreaming {
  val appender = LogbackUtils.appender[BasicBoundedReplayRxAppender]("RX")
    .getOrElse(new BasicBoundedReplayRxAppender)
  val subscriptions: ItemMap[WebSocketClient, Subscription] =
    StmItemMap.empty[WebSocketClient, Subscription]

  override def authenticateAsync(req: RequestHeader): Future[AuthResult] = auth(req)

  override def openSocketCall: Call = routes.MetaOAuth.openSocket()
}
