package controllers

import akka.stream.Materializer
import com.malliina.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.malliina.maps.{ItemMap, StmItemMap}
import com.malliina.play.controllers.LogStreaming
import com.malliina.play.http.AuthedRequest
import play.api.mvc.{Call, RequestHeader}
import rx.lang.scala.Subscription

import scala.concurrent.Future

class LogStreamer(auth: RequestHeader => Future[AuthedRequest],
                  val mat: Materializer,
                  isProd: Boolean)
  extends LogStreaming(mat) {
  val appender = LogbackUtils.appender[BasicBoundedReplayRxAppender]("RX")
    .getOrElse(new BasicBoundedReplayRxAppender)
  val subscriptions: ItemMap[Client, Subscription] =
    StmItemMap.empty[Client, Subscription]

  override def authenticateAsync(req: RequestHeader): Future[AuthedRequest] = auth(req)

  override def openSocketCall: Call = routes.MetaOAuth.openSocket()

  override def wsUrl(request: RequestHeader): String =
    openSocketCall.webSocketURL(secure = isProd)(request)
}
