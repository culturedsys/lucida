package training

import org.apache.spark.SparkContext
import org.scalatest.FunSuite
import RDDFunctions._

/**
  * Tests for functions added to the RDD
  */
class RDDFunctionsSuite extends FunSuite {
  val sc = new SparkContext("local[1]", "RDDFunctionsSuite")

  val alphabet = sc.parallelize('a' to 'z')

  test("Extract should return two RDDs containing together all elements of original") {
    val (extracted, rest) = alphabet.extract(10, 1)
    assert(extracted.count + rest.count === alphabet.count)
    alphabet.collect.map { c =>
      val inExtracted = extracted.filter(_ == c).count != 0
      val inRest = rest.filter(_ == c).count != 0

      assert(inExtracted || inRest, s": $c was neither in extracted nor in rest")
    }
  }

  test("Extract 0 should extract the first subsample") {
    val (extracted, rest) = alphabet.extract(10, 0)
    assert(extracted.collect.toSeq === Seq('a', 'b'))
  }

  test("Extract 2 should extract the third subsample") {
    val (extracted, rest) = alphabet.extract(10, 2)
    assert(extracted.collect.toSeq === Seq('e', 'f'))
  }

  test("Extract 2 should not leave the third subsample in the rest RDD") {
    val (extracted, rest) = alphabet.extract(10, 2)
    val restSeq = rest.collect.toSeq
    assert(!restSeq.contains('e') && !restSeq.contains('f'))
  }
}
