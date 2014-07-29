package com.mle.http

import java.nio.file.{Path, Paths}

import com.mle.oauth.GoogleOAuthCredentials
import com.mle.util.BaseConfigReader
import com.mle.util.FileImplicits.StorageFile

/**
 * @author Michael
 */
object Info {
  val discoGsAuthReader = new BaseConfigReader[DiscoGsOAuthCredentials] {
    val defaultHomePath = userHome / "keys" / "discogs-oauth.txt"

    override def userHomeConfPath: Path = sys.props.get("discogs.oauth").map(Paths.get(_)) getOrElse defaultHomePath

    override def fromMapOpt(map: Map[String, String]): Option[DiscoGsOAuthCredentials] = for {
      cKey <- map get "consumerKey"
      cSecret <- map get "consumerSecret"
      aToken <- map get "accessToken"
      aTokenSecret <- map get "accessTokenSecret"
    } yield DiscoGsOAuthCredentials(cKey, cSecret, aToken, aTokenSecret)
  }
  val googleAuthReader = new BaseConfigReader[GoogleOAuthCredentials] {
    val defaultHomePath = userHome / "keys" / "google-oauth.txt"

    override def userHomeConfPath: Path = sys.props.get("google.oauth").map(Paths.get(_)) getOrElse defaultHomePath

    override def fromMapOpt(map: Map[String, String]): Option[GoogleOAuthCredentials] = for {
      clientId <- map get "clientId"
      clientSecret <- map get "clientSecret"
      scope <- map get "scope"
    } yield GoogleOAuthCredentials(clientId, clientSecret, scope)
  }
}
