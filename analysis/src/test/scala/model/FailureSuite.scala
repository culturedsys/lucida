package model

import org.scalatest.FunSuite

/**
  * Test that failures are correctly reported.
  */
class FailureSuite extends FunSuite {
  test("Opening a non-doc file in the DocExtractor is reported") {
    val result = DocExtractor.extract(getClass.getResourceAsStream("features.docx"))
    assert(result.isFailure)
  }
}
