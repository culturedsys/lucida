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

  // Contains what might be a subsection number, e.g 1.1
  possibleSubhead: Boolean,

  // Contains what might be a subsubsection number, e.g. 1.1.1
  possibleSubsubhead: Boolean,

  // Contains @
  possibleEmail: Boolean,

  // Contains www or http
  possibleWeb: Boolean,

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