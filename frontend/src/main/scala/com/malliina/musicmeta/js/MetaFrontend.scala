package com.malliina.musicmeta.js

import com.malliina.http.FullUrl
import org.scalajs.dom
import org.scalajs.dom.html.TableSection
import org.scalajs.dom.raw.{ErrorEvent, Event, MessageEvent}
import org.scalajs.dom.{CloseEvent, WebSocket, document}
import play.api.libs.json.{JsError, JsValue, Json}
import scalatags.JsDom.all._

import scala.util.Try

object MetaFrontend {
  def main(args: Array[String]): Unit = {
    new MetaSocket
  }
}

case class LogEvent(timeStamp: Long,
                    timeFormatted: String,
                    message: String,
                    loggerName: String,
                    threadName: String,
                    level: String) {
  def isError = level == "ERROR"
}

object LogEvent {
  implicit val json = Json.format[LogEvent]
}

case class LogEvents(events: Seq[LogEvent])

object LogEvents {
  implicit val json = Json.format[LogEvents]
}

class MetaSocket {
  val tableContent = document.getElementById("log-table-body").asInstanceOf[TableSection]
  val socket = openSocket("/ws?f=json")

  def onMessage(msg: MessageEvent): Unit = {
    Try(Json.parse(msg.data.toString)).map { json =>
      val isPing = (json \ "event").validate[String].filter(_ == "ping").isSuccess
      if (!isPing) {
        handlePayload(json)
      }
    }.recover { case e => onJsonFailure(e) }
  }

  def handlePayload(value: JsValue) = {
    value.validate[LogEvents].fold(err => onJsonFailure(JsError(err)), prependAll)
  }

  def onJsonFailure(error: Any) = {
    println(error)
  }

  def prependAll(events: LogEvents) = events.events foreach prepend

  def prepend(event: LogEvent) =
    Option(tableContent.firstChild).map { first =>
      tableContent.insertBefore(row(event).render, first)
    }.getOrElse {
      tableContent.appendChild(row(event).render)
    }

  def row(event: LogEvent) = {
    val opening = if (event.isError) tr(`class` := "danger") else tr
    opening(td(event.timeFormatted), td(event.message), td(event.loggerName), td(event.level))
  }

  def onConnected(e: Event): Unit = updateStatus("Connected.")

  def onClosed(e: CloseEvent): Unit = updateStatus("Closed.")

  def onError(e: ErrorEvent): Unit = updateStatus("Error.")

  def updateStatus(status: String) = {
    document.getElementById("status").innerHTML = status
  }

  def openSocket(pathAndQuery: String): WebSocket = {
    val url = wsBaseUrl.append(pathAndQuery)
    val socket = new dom.WebSocket(url.url)
    socket.onopen = (e: Event) => onConnected(e)
    socket.onmessage = (e: MessageEvent) => onMessage(e)
    socket.onclose = (e: CloseEvent) => onClosed(e)
    socket.onerror = (e: ErrorEvent) => onError(e)
    socket
  }

  def wsBaseUrl: FullUrl = {
    val location = dom.window.location
    val wsProto = if (location.protocol == "http:") "ws" else "wss"
    FullUrl(wsProto, location.host, "")
  }
}
