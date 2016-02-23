package tests

import java.nio.file.Paths

import org.specs2.mutable.Specification

class OtherTests extends Specification {
  "tests" should {
    "run" in {
      1 mustEqual 1
    }

    "read a path" in {
      val p = Paths.get("é")
      val f = p.toFile
      p.toAbsolutePath.toString mustEqual f.getAbsolutePath
    }
  }
}
