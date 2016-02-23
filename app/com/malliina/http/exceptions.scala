package com.malliina.http

import play.api.libs.ws.WSResponse

class ResponseException(val response: WSResponse, val url: String)
  extends MusicException(s"Request to $url failed. Invalid response code ${response.status}. Body ${response.body}.") {
  def code = response.status
  def body = response.body
}

class CoverNotFoundException(msg: String) extends MusicException(msg)

class MusicException(msg: String) extends Exception(msg)
