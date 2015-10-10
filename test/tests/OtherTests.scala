package tests

import java.nio.file.Paths

import org.scalatest.FunSuite

/**
 * @author Michael
 */
class OtherTests extends FunSuite {
  test("yo") {
    assert(1 === 1)
  }

  test("file") {
    val p = Paths.get("é")
    val f = p.toFile
    assert(p.toAbsolutePath.toString === f.getAbsolutePath)
  }
}
