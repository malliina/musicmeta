package com.malliina.http

import java.io.{BufferedOutputStream, Closeable}
import java.nio.file.{Files, Path}

import com.malliina.concurrent.ExecutionContexts
import com.malliina.http.DiscoClient.log
import com.malliina.oauth.DiscoGsOAuthCredentials
import com.malliina.storage._
import com.malliina.util.Util
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.client.methods.HttpGet
import play.api.Logger
import play.api.http.HeaderNames
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

object DiscoClient {
  private val log = Logger(getClass)
  val RESULTS = "results"
  val ID = "id"
  val IMAGES = "images"
  val URI = "uri"

  def apply(creds: DiscoGsOAuthCredentials, coverDir: Path): DiscoClient =
    new DiscoClient(creds, coverDir)(ExecutionContexts.cached)
}

class DiscoClient(credentials: DiscoGsOAuthCredentials, coverDir: Path)(implicit ec: ExecutionContext) extends Closeable {
  Files.createDirectories(coverDir)
  val httpClient = new AsyncHttp()
  val consumerKey = credentials.consumerKey
  val consumerSecret = credentials.consumerSecret
  val iLoveDiscoGsFakeCoverSize = 15378

  /** Returns the album cover. Optionally downloads and caches it if it doesn't already exist locally.
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

  /** Streams `url` to `file`.
    *
    * @param url  url to download
    * @param file destination path
    * @return the size of the downloaded file, stored in `file`
    * @see http://www.playframework.com/documentation/2.6.x/ScalaWS
    */
  protected def downloadFile(url: String, file: Path): Future[StorageSize] = {
    authenticated(url).map { r =>
      Util.using(new BufferedOutputStream(Files.newOutputStream(file))) { outStream =>
        r.inner.getEntity.writeTo(outStream)
      }
      Files.size(file).bytes
    }
    //    authenticated(url).withMethod("GET").stream().flatMap { stream =>
    //      stream.bodyAsSource.runWith(Streams.fileWriter(file)).map(_.count.bytes)
    //    }
  }

  protected def coverFile(artist: String, album: String): Path = {
    // avoids platform-specific file system encoding nonsense
    val hash = DigestUtils.md5Hex(s"$artist-$album")
    coverDir resolve s"$hash.jpg"
  }

  /** Downloads the album cover of `artist`s `album`.
    *
    * Performs three web requests in sequence to the DiscoGs API:
    *
    * 1) Obtains the album ID
    * 2) Obtains the album details (with the given album ID)
    * 3) Downloads the album cover (the URL of which is available in the details)
    *
    * At least the last step, which downloads the cover, requires OAuth authentication.
    *
    * @param artist    the artist
    * @param album     the album
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

  private def getAlbumIdContent(url: String): Future[Long] =
    downloadString(url).map(content => albumId(content)
      .getOrElse(throw new CoverNotFoundException(s"Unable to find album id from response: $content")))

  private def getCoverUrl(id: Long): Future[String] =
    downloadString(albumUrl(id)).map(content => coverUrl(content)
      .getOrElse(throw new CoverNotFoundException(s"Unable to find cover art URL from response: $content")))

  private def downloadString(url: String): Future[String] = getResponse(url).map(_.asString)

  private def getResponse(url: String): Future[WebResponse] = authenticated(url)
    .flatMap(r => validate(r, url).fold(Future.successful(r))(Future.failed))

  private def authenticated(url: String) = {
    log debug s"Preparing authenticated request to $url"
    val builder = new HttpGet(url)
    builder.addHeader(HeaderNames.AUTHORIZATION, s"Discogs key=$consumerKey, secret=$consumerSecret")
    httpClient.execute(builder)
    //    WS.clientUrl(url) sign signer
    //    client.url(url).addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Discogs key=$consumerKey, secret=$consumerSecret")
  }

  private def albumIdUrl(artist: String, album: String): String = {
    val artistEnc = WebUtils.encodeURIComponent(artist)
    val albumEnc = WebUtils.encodeURIComponent(album)
    s"https://api.discogs.com/database/search?artist=$artistEnc&release_title=$albumEnc"
  }

  private def albumUrl(albumId: Long) = s"https://api.discogs.com/releases/$albumId"

  private def validate(wsResponse: WebResponse, url: String): Option[Exception] = {
    val code = wsResponse.code
    code match {
      case c if (c >= 200 && c < 300) || c == 404 => None
      case _ => Option(new ResponseException(wsResponse, url))
    }
  }

  import DiscoClient._

  private def albumId(responseContent: String): Option[Long] =
    (Json.parse(responseContent) \ RESULTS \\ ID).headOption.flatMap(_.asOpt[Long])

  private def coverUrl(responseContent: String): Option[String] =
    (Json.parse(responseContent) \ IMAGES \\ URI).headOption.flatMap(_.asOpt[String])

  def close() = httpClient.close()
}
