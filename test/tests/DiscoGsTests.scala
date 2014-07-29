package tests

import com.mle.http.{DiscoClient, Info}
import com.mle.util.FileUtilities
import com.mle.util.Utils.executionContext
import org.scalatest.FunSuite
import org.scalatestplus.play.OneAppPerSuite

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 *
 * @author mle
 */
class DiscoGsTests extends FunSuite with OneAppPerSuite {
  val uri = "http://api.discogs.com/image/R-5245462-1388609959-3809.jpeg"

  test("can download cover") {
    val client = new DiscoClient(Info.discoGsAuthReader.load, FileUtilities.tempDir)
    val result = client.downloadCover("Iron Maiden", "Powerslave").map(p => s"Downloaded to $p").recover {
      case t => s"Failure: $t"
    }
    val r = Await.result(result, 5 seconds)
    println(r)
  }
}
