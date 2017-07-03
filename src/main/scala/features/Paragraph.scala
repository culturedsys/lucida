package features

/**
  * A class representing the salient properties of a paragraph
  */
case class Paragraph(
  //A string used to identify the paragraph to human readers (it has no significance to the
  // model); it should probably be the text of the paragraph, or part of it.
  description: String,

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

  isSameAsPrevious: Boolean,

  // The tag assigned to the paragraph, if any
  tag: Option[Tag]
)


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