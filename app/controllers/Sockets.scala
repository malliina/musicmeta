package controllers

import com.mle.logbackrx.{BasicBoundedReplayRxAppender, LogbackUtils}
import com.mle.play.controllers.{AuthResult, LogStreaming}
import com.mle.play.ws.SyncAuth
import com.mle.util.Log
import play.api.mvc._

/**
 *
 * @author mle
 */
object Sockets extends Controller with LogStreaming with SyncAuth with Log {
  lazy val appender = LogbackUtils.getAppender[BasicBoundedReplayRxAppender]("RX")

  override def openSocketCall: Call = routes.Sockets.openSocket()

  override def authenticate(implicit req: RequestHeader): Option[AuthResult] = MetaOAuth.authenticate(req)
}
