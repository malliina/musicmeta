package tests

import com.mle.concurrent.ExecutionContexts.cached
import com.mle.file.FileUtilities
import com.mle.http.DiscoClient
import com.mle.oauth.DiscoGsOAuthReader
import org.scalatest.FunSuite
import org.scalatestplus.play.OneAppPerSuite

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 *
 * @author mle
 */
class DiscoGsTests extends FunSuite with OneAppPerSuite {
  val uri = "http://api.discogs.com/image/R-5245462-1388609959-3809.jpeg"

  test("can download cover") {
    val client = new DiscoClient(DiscoGsOAuthReader.load, FileUtilities.tempDir)
    val result = client.downloadCover("Iron Maiden", "Powerslave").map(p => s"Downloaded to $p").recover {
      case t => s"Failure: $t"
    }
    val r = Await.result(result, 5.seconds)
    println(r)
  }
}
