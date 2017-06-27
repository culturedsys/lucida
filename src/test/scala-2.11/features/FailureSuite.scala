package features

import org.scalatest.FunSuite

/**
  * Test that failures are correctly reported.
  */
class FailureSuite extends FunSuite {
  test("Opening a non-doc file is reported") {
    val result = FeatureExtractor.extract(getClass.getResourceAsStream("features.docx"))
    assert(result.isFailure)
  }
}
