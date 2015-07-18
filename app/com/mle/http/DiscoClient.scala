package com.mle.http

import java.io.Closeable
import java.nio.file.{Files, Path}

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.oauth.DiscoGsOAuthCredentials
import com.mle.play.streams.Streams
import com.mle.storage._
import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.json.Json
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.Future


/**
 * @author mle
 */
class DiscoClient(credentials: DiscoGsOAuthCredentials, coverDir: Path) extends Closeable {
  Files.createDirectories(coverDir)
  implicit val client = new NingWSClient(new AsyncHttpClientConfig.Builder().build())
  val key = ConsumerKey(credentials.consumerKey, credentials.consumerSecret)
  val accessToken = RequestToken(credentials.accessToken, credentials.accessTokenSecret)
  val signer = OAuthCalculator(key, accessToken)
  val iLoveDiscoGsFakeCoverSize = 15378

  /**
   * Returns the album cover. Optionally downloads and caches it if it doesn't already exist locally.
   *
   * Fails with a [[NoSuchElementException]] if the cover cannot be found. Can also fail with a [[java.io.IOException]]
   * and a [[com.fasterxml.jackson.core.JsonParseException]].
   *
   * @return the album cover file, which is an image
   */
  def cover(artist: String, album: String): Future[Path] = {
    val file = coverFile(artist, album)
    if (Files.isReadable(file) && Files.size(file) != iLoveDiscoGsFakeCoverSize) Future.successful(file)
    else downloadCover(artist, album).filter(f => Files.size(f) != iLoveDiscoGsFakeCoverSize)
  }

  def downloadCover(artist: String, album: String): Future[Path] =
    downloadCover(artist, album, _ => coverFile(artist, album))

  /**
   * Streams `url` to `file`.
   *
   * @param url url to download
   * @param file destination path
   * @return the size of the downloaded file, stored in `file`
   * @see  http://www.playframework.com/documentation/2.3.x/ScalaWS
   */
  protected def downloadFile(url: String, file: Path): Future[StorageSize] = {
    signedRequest(url).get(headers => Streams.fileWriter(file)).flatMap(_.run).map(_.bytes)
  }

  protected def coverFile(artist: String, album: String): Path = coverDir resolve s"$artist-$album.jpg"

  /**
   * Downloads the album cover of `artist`s `album`.
   *
   * Performs three web requests in sequence to the DiscoGs API:
   *
   * 1) Obtains the album ID
   * 2) Obtains the album details (with the given album ID)
   * 3) Downloads the album cover (the URL of which is available in the details)
   *
   * At least the last step, which downloads the cover, requires OAuth authentication.
   *
   * @param artist the artist
   * @param album the album
   * @param urlToFile the file to download the cover to, given its remote URL
   * @return the downloaded album cover along with the number of bytes downloaded
   */
  protected def downloadCover(artist: String, album: String, urlToFile: String => Path): Future[Path] =
    for {
      id <- getAlbumIdContent(albumIdUrl(artist, album))
      url <- getCoverUrl(id)
      file = urlToFile(url)
      bytes <- downloadFile(url, file)
    } yield file

  private def downloadString(url: String): Future[String] = getResponse(url) map (_.body)

  private def getResponse(url: String): Future[WSResponse] = signedRequest(url).get()

  private def signedRequest(url: String) = WS.clientUrl(url) sign signer

  private def albumIdUrl(artist: String, album: String): String = {
    val artistEnc = WebUtils.encodeURIComponent(artist)
    val albumEnc = WebUtils.encodeURIComponent(album)
    s"http://api.discogs.com/database/search?artist=$artistEnc&release_title=$albumEnc"
  }

  private def albumUrl(albumId: Long) = s"http://api.discogs.com/releases/$albumId"

  private def getAlbumIdContent(url: String): Future[Long] =
    downloadString(url).map(content => albumId(content)
      .getOrElse(throw new NoSuchElementException(s"Unable to find album id from response: $content")))

  private def getCoverUrl(id: Long): Future[String] =
    downloadString(albumUrl(id)).map(content => coverUrl(content)
      .getOrElse(throw new NoSuchElementException(s"Unable to find cover art URL from response: $content")))

  import com.mle.http.DiscoClient._

  private def albumId(responseContent: String): Option[Long] =
    (Json.parse(responseContent) \ RESULTS \\ ID).headOption.flatMap(_.asOpt[Long])

  private def coverUrl(responseContent: String): Option[String] =
    (Json.parse(responseContent) \ IMAGES \\ URI).headOption.flatMap(_.asOpt[String])

  def close() = client.close()
}

object DiscoClient {
  val RESULTS = "results"
  val ID = "id"
  val IMAGES = "images"
  val URI = "uri"
}

