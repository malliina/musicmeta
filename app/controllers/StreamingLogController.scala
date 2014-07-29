package controllers

import java.util.concurrent.ConcurrentHashMap

import com.mle.logbackrx.LogEvent
import com.mle.musicmeta.SimpleCommand
import com.mle.play.ws.WebSocketController
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.RequestHeader
import play.api.mvc.WebSocket.FrameFormatter
import rx.lang.scala.{Observable, Subscription}

import scala.collection.JavaConversions._

/**
 *
 * @author mle
 */
trait StreamingLogController extends WebSocketController {
  def logEvents: Observable[LogEvent]

  private val jsonLogEvents = logEvents.map(e => Json.toJson(e))

  private val subscriptions: collection.concurrent.Map[WebSocketClient, Subscription] =
    new ConcurrentHashMap[WebSocketClient, Subscription]()

  val SUBSCRIBE = "subscribe"
  override type Client = WebSocketClient
  override type Message = JsValue

  def openLogSocket = ws(FrameFormatter.jsonFrame)

  override def newClient(user: String, channel: Channel[Message])(implicit request: RequestHeader): Client =
    WebSocketClient(user, channel, request)

  override def onMessage(msg: Message, client: Client): Unit = {
    msg.validate[SimpleCommand].map(_.cmd match {
      case SUBSCRIBE =>
        val subscription = jsonLogEvents.subscribe(e => client.controlChannel push e)
        subscriptions += (client -> subscription)
        writeLog(client, s"subscribed. Subscriptions in total: ${subscriptions.size}")
      case _ => log.warn(s"Unknown message: $msg")
    })
  }

  override def onConnect(client: Client): Unit =
    writeLog(client, "connected")

  override def onDisconnect(client: Client): Unit = {
    subscriptions.get(client).foreach(_.unsubscribe())
    subscriptions -= client
    writeLog(client, "disconnected")
  }

  private def writeLog(client: Client, suffix: String): Unit =
    log.info(s"User: ${client.user} from: ${client.request.remoteAddress} $suffix.")
}

trait WebSocketClientBase {
  def user: String

  def controlChannel: Channel[JsValue]
}

case class WebSocketClient(user: String, controlChannel: Channel[JsValue], request: RequestHeader) extends WebSocketClientBase

