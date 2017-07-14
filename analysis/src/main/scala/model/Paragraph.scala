package model

/**
  * A class representing the salient properties of a paragraph
  */
case class Paragraph(
  //A string used to identify the paragraph to human readers (it has no significance to the
  // model); it should probably be the text of the paragraph, or part of it.
  description: String,

  // Features of the first N words
  words: Seq[Word],

  // Location in document (as 8ths of total length)
  location: Int,

  // A hint as to what kind of paragraph this might be, on the basis of the presence of numbers
  numberHint: NumberHint,

  // A hint as to whether this paragraph contains a network reference, based on the presence of @
  // symbols and common web address elements.
  netHint: NetHint,

  // Length in words (this is the length of the whole paragraph, rather than the length of
  // a single line, as it is for Luoung et al.)
  length: Int,

  // The font size, in points sizes smaller than the largest in document, or Small if
  // smaller than the majority size of the document
  fontSize: FontSize,

  isBold: Boolean,

  isItalic: Boolean,

  isBullet: Boolean,

  // Not entirely sure if word treats pictures and tables as paragraphs; these two
  // attributes may be unnecessary
  // isPicture: Boolean,
  // isTable: Boolean,

  // Does this paragraph have the same font size, family, bold, italic, and alignment as
  // the previous?

  isSameAsPrevious: Boolean
)

object Paragraph {

  /**
    * The number of words to sample from each paragraph. Following Luoung et al., this is set to 4.
    */
  val WORDS = 4

  /**
    * Paragraph position in document is scaled so that it is in a number of "bins" (rather than
    * using absolute position, which would obviously not compare well between different length
    * documents). Luoung et al. use 8 bins, which they report gave good results in experiments.
    */
  val POSITION_BINS = 8

  /**
    * Luong et al. only specifically pay attention to the largest three font sizes - everything
    * else is grouped together as "Smaller", "Common", or "Larger". This constant controls
    * how many of the largest sizes to pay attention to.
    */
  val DISTINCT_SIZES = 3

  /**
    * Luong et al. treat all lengths of 5 or longer as equivalent
    */
  val LENGTH_MAX = 5

}

/**
  * Represents a font size as a relative size.
  */
sealed trait FontSize

/**
  * The most common font size in the document.
  */
case object Common extends FontSize

/**
  * Any size smaller than the most common size.
  */
case object Smaller extends FontSize

/**
  * A size relative to the largest font size. RelativeSize(0) is the largest size,
  * RelativeSize(1) the second largest size, etc. Luong et al. only record relative size data for
  * the three largest font sizes. Everything else is recorded as a generic Larger size.
  */
final case class RelativeSize(size: Int) extends FontSize

case object Larger extends FontSize

object FontSize {
  implicit def ordering[A <: FontSize]: Ordering[A] = Ordering.fromLessThan { (x, y) =>
    (x, y) match {
      // A RelativeSize n is the nth largest size, so one RelativeSize is smaller than another only
      // if its index is *larger* (e.g., the 2nd largest size is smaller than the 1st largest).
      case (RelativeSize(a), RelativeSize(b)) => a > b

      // Anything that is not a RelativeSize is smaller than any RelativeSize
      case (_, RelativeSize(_)) => true

      // Larger is only smaller than RelativeSize, already handled above
      // Common is smaller than RelativeSize (handled above) and Larger
      case (Common, Larger) => true

      // Smaller is smaller than RelativeSize (handled above), Common, and Larger
      case (Smaller, Common) => true
      case (Smaller, Larger) => true

      // The above are all the cases in which one size is smaller than another, so the remainder
      // must be false.
      case _ => false
    }
  }
}

/**
  * Represents information that may be relevant, based on the presence of certain numeric patterns
  */
sealed trait NumberHint
case object NumberOther extends NumberHint
case object PossibleSubsection extends NumberHint
case object PossibleSubsubsection extends NumberHint

/**
  * Represents information that may be relevant to whether the paragraph represents a network
  * address.
  */
sealed trait NetHint
case object NetOther extends NetHint
case object PossibleEmail extends NetHint
case object PossibleWeb extends NetHint

/**
  * A class representing the salient properties of a word
  */
case class Word(
  word: String,
  lowerCase: String,
  unpunctuated: String,
  wordCase: WordCase,
  digits: Digits
  ) {
}

object Word {
  def apply(word: String): Word = {
    val lowerCase = word.toLowerCase
    val unpunctuated = lowerCase.filter(_.isLetterOrDigit)
    val wordCase =
      if (word.forall(_.isUpper))
        AllCaps
      else if (word.substring(1).exists(_.isUpper))
        MixedCaps
      else if (word(0).isUpper)
        InitialCaps
      else
        OtherCaps
    val digitCount = word.count(_.isDigit)
    val digits =
      if (digitCount == word.length)
        JustDigits(math.min(4, digitCount))
      else if (digitCount > 0)
        HasDigits
      else
        NoDigits


    Word(word, lowerCase, unpunctuated, wordCase, digits)
  }
}

/**
  * Represents the capitalization of a word:
  *
  * AllCaps - entire token is capital letters
  * MixedCaps - some upper case and some lower case
  * InitialCaps - first character is capital, rest lower
  * OtherCaps - anything which does not fit
  */
sealed trait WordCase
case object AllCaps extends WordCase
case object InitialCaps extends WordCase
case object MixedCaps extends WordCase
case object OtherCaps extends WordCase

/**
  * Represents the presence of numbers
  *
  * JustDigits(n) - solely digits, and n (up to a maximum of 4) of them
  * HasDigits - contain digits along with non-digit characters
  * NoDigits - no digits
  * OtherDigits - some other condition
  */
sealed trait Digits
final case class JustDigits(count: Int) extends Digits
case object HasDigits extends Digits
case object NoDigits extends Digits
case object OtherDigits extends Digits

class TaggedParagraph(
   description: String,
   words: Seq[Word],
   location: Int,
   numberHint: NumberHint,
   netHint: NetHint,
   length: Int,
   fontSize: FontSize,
   isBold: Boolean,
   isItalic: Boolean,
   isBullet: Boolean,
   isSameAsPrevious: Boolean,

   // The tag assigned to the paragraph, if any
   val tag: Tag
                     ) extends Paragraph(description, words, location, numberHint, netHint, length, fontSize, isBold,
  isItalic, isBullet, isSameAsPrevious)

object TaggedParagraph extends {
  def apply(
      description: String,
      words: Seq[Word],
      location: Int,
      numberHint: NumberHint,
      netHint: NetHint,
      length: Int,
      fontSize: FontSize,
      isBold: Boolean,
      isItalic: Boolean,
      isBullet: Boolean,
      isSameAsPrevious: Boolean,
      tag: Tag): TaggedParagraph =
    new TaggedParagraph(
      description,
      words,
      location,
      numberHint,
      netHint,
      length,
      fontSize,
      isBold,
      isItalic,
      isBullet,
      isSameAsPrevious,
      tag
    )

  def addTag(para: Paragraph, tag: Tag): TaggedParagraph = {
    val Paragraph(description, words, location, numberHint, netHint, length, fontSize, isBold,
          isItalic,isBullet, isSameAsPrevious) = para
    TaggedParagraph(description, words, location, numberHint, netHint, length, fontSize, isBold,
      isItalic, isBullet, isSameAsPrevious, tag)
  }

}