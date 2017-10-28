package tests

import com.malliina.concurrent.ExecutionContexts.cached
import com.malliina.concurrent.FutureOps
import com.malliina.file.FileUtilities
import com.malliina.http.DiscoClient
import com.malliina.oauth.DiscoGsOAuthReader
import com.malliina.util.Util
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class DiscoGsTests extends FunSuite {
  val uri = "http://api.discogs.com/image/R-5245462-1388609959-3809.jpeg"

  ignore("download cover") {
    Util.using(new DiscoClient(DiscoGsOAuthReader.load, FileUtilities.tempDir)) { client =>
      val result = client.downloadCover("Iron Maiden", "Powerslave")
        .map(p => s"Downloaded to $p")
        .recoverAll(t => s"Failure: $t")
      val r = Await.result(result, 20.seconds)

      assert(r startsWith "Downloaded")
    }
  }
}
