package model

import org.scalatest.FunSuite

/**
  * Tests for the utility functions for representing feature templates.
  */
class FeatureTemplateSuite extends FunSuite {
  val features = Seq(
    Unigram[Any](_ => "one"),
    Unigram[Any](_ => "two", -1),
    Bigram[Any](_ => "three", 2)
  )

  test("Number of feature template strings should be same as number of feature templates") {
    assert(features.size === FeatureTemplate.templatesAsStrings(features).length)
  }

  test("Each feature template should refer to the attribute with the same index") {
    FeatureTemplate.templatesAsStrings(features).zipWithIndex.foreach { case (template, index) =>
      assert(template.split(",")(1).startsWith(index.toString))
    }
  }

  test("The feature template string should contain the correct relative position") {
    val strings = FeatureTemplate.templatesAsStrings(features).map(_.split(":")(1))
    assert(strings(0).startsWith("%x[0"))
    assert(strings(1).startsWith("%x[-1"))
    assert(strings(2).startsWith("%x[2"))
  }

  test("Unigram templates should start with U") {
    val strings = FeatureTemplate.templatesAsStrings(features)
    assert(strings(0).startsWith("U"))
    assert(strings(1).startsWith("U"))
  }

  test("Bigram templates should start with U") {
    assert(FeatureTemplate.templatesAsStrings(features)(2).startsWith("B"))
  }

  test("When unqualifiedBigram is specified, an additional bigram template string is generated") {
    val strings = FeatureTemplate.templatesAsStrings(features, unqualifiedBigram = true)
    assert(strings.length === 4)
    assert(strings.contains("B"))
  }

  test("Generated tokens contain the extracted values in order") {
    val attributes = FeatureTemplate.tokensAsStrings(features, Seq(null)).head
    assert(attributes.zip(Array("one", "two", "three")).forall { case (a, b) => a === b })
  }
}
