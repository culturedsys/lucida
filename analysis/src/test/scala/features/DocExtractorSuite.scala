package features

import org.scalatest.FunSuite

/**
  * Test that the attributes reported by the DocExtractor match the attributes manually identified
  * in a test document.
  */
class DocExtractorSuite extends FunSuite {

  val source = getClass.getResourceAsStream("features.doc")

  assume(source != null)

  val extracted = DocExtractor.extract(source)

  assume(extracted.isSuccess)

  val paragraphs = extracted.get

  test("Paragraph 0 should be a possibleSubhead") {
    assert(paragraphs(0).numberHint === PossibleSubsection)
  }

  test("Paragraph 1 should be a possibleSubsubhead") {
    assert(paragraphs(1).numberHint === PossibleSubsubsection)
  }

  test("Paragraph 1 should not be a possibleEmail") {
    assert(paragraphs(1).netHint !== PossibleEmail)
  }

  test("Paragraph 2 should be a possibleEmail") {
    assert(paragraphs(2).netHint === PossibleEmail)
  }

  test("Paragraph 2 should not be a possibleWeb") {
    assert(paragraphs(2).netHint !== PossibleWeb)
  }

  test("Paragraph 3 should be a possibleWeb") {
    assert(paragraphs(3).netHint === PossibleWeb)
  }

  test("Paragraph 4 should be a possibleWeb") {
    assert(paragraphs(4).netHint === PossibleWeb)
  }

  test("Paragraph 5's length of 8 should be capped at 5") {
    assert(paragraphs(5).length === 5)
  }

  test("Paragraph 6 should have largest font size") {
    assert(paragraphs(6).fontSize === RelativeSize(0))
  }

  test("Paragraph 7 should have second largest") {
    assert(paragraphs(7).fontSize === RelativeSize(1))
  }

  test("Paragraph 8 should have third largest font size") {
    assert(paragraphs(8).fontSize === RelativeSize(2))
  }

  test("Paragraph 9 should have a fontSize of Larger") {
    assert(paragraphs(9).fontSize === Larger)
  }

  test("Paragraph 10 should have a fontSize of Common") {
    assert(paragraphs(10).fontSize === Common)
  }

  test("Paragraph 11 should have a fontSize of Smaller") {
    assert(paragraphs(11).fontSize === Smaller)
  }

  test("Paragraph 12 should have a fontSize of Smaller") {
    assert(paragraphs(12).fontSize === Smaller)
  }

  test("Paragraph 12 should not be bold") {
    assert(!paragraphs(12).isBold)
  }

  test("Paragraph 13 should be bold") {
    assert(paragraphs(13).isBold)
  }

  test("paragraph 13 should not be italic") {
    assert(!paragraphs(13).isItalic)
  }

  test("Paragraph 14 should be italic") {
    assert(paragraphs(14).isItalic)
  }

  test("Parahraph 14 should not be a bullet") {
    assert(!paragraphs(14).isBullet)
  }

  test("Paragraph 15 should be a bullet") {
    assert(paragraphs(15).isBullet)
  }

  // Paragraph 16 is not directly tested, but is used as a baseline for the subsequent paragraphs

  test("Paragraph 17 should be the same as previous") {
    assert(paragraphs(17).isSameAsPrevious)
  }

  test("Paragraph 18 should not be the same as previous") {
    assert(!paragraphs(18).isSameAsPrevious)
  }

  test("Paragraph 19 should not be the same as previous") {
    assert(!paragraphs(19).isSameAsPrevious)
  }

  test("Paragraph 20 should not be the same as previous") {
    assert(!paragraphs(20).isSameAsPrevious)
  }

  test("Paragraph 21 should not be the same as previous") {
    assert(!paragraphs(21).isSameAsPrevious)
  }

  test("Paragraph 22 should not be the same as previous") {
    assert(!paragraphs(22).isSameAsPrevious)
  }

  test("Paragraph 23 should not be the same as previous") {
    assert(!paragraphs(23).isSameAsPrevious)
  }

  test("Paragraph 24 should be bold (via a style)") {
    assert(paragraphs(24).isBold)
  }

  test("Paragraph 25 should be italic (via a style)") {
    assert(paragraphs(25).isItalic)
  }

  test("Paragraph 26 should have fontSize 0 (via a style") {
    assert(paragraphs(26).fontSize == RelativeSize(0))
  }

  test("Paragraph 27's first word should be 'Initcap!'") {
    assert(paragraphs(27).words(0).word === "Initcap!")
  }

  test("Paragraphs 27's first word in lower case should be 'initcap!'") {
    assert(paragraphs(27).words(0).lowerCase === "initcap!")
  }

  test("Paragraph 27's first word without punctuation should be 'initcap'") {
    assert(paragraphs(27).words(0).unpunctuated === "initcap")
  }

  test("Paragraph 27's first word should be in initial caps") {
    assert(paragraphs(27).words(0).wordCase === InitialCaps)
  }

  test("Paragraph 27's second word should be in all caps") {
    assert(paragraphs(27).words(1).wordCase === AllCaps)
  }

  test("Paragraph 27's third word should be in mixed caps") {
    assert(paragraphs(27).words(2).wordCase === MixedCaps)
  }

  test("Paragraph 27's fourth word should be in other caps") {
    assert(paragraphs(27).words(3).wordCase === OtherCaps)
  }

  test("Paragraph 28's first word has digits") {
    assert(paragraphs(28).words(0).digits == HasDigits)
  }

  test("Paragraph 28's second word has 1 digit") {
    assert(paragraphs(28).words(1).digits === JustDigits(1))
  }

  test("Paragraph 28's third word has 2 digits") {
    assert(paragraphs(28).words(2).digits === JustDigits(2))
  }

  test("Paragraph 28's fourth word has 3 digits") {
    assert(paragraphs(28).words(3).digits === JustDigits(3))
  }

  test("Paragraph 29's first word has 4 digits") {
    assert(paragraphs(29).words(0).digits === JustDigits(4))
  }

  test("Paragraph 29's second word has more than 4 digits, capped to 4") {
    assert(paragraphs(29).words(1).digits === JustDigits(4))
  }

  test("Paragraph 29's third word is EMPTY") {
    assert(paragraphs(29).words(2).word === "EMPTY")
  }
}
