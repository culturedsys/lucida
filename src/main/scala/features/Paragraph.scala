package features

/**
  * A class representing the salient properties of a paragraph
  */
case class Paragraph(
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

  // For formatting properties, I only consider formats that apply to the whole paragraph
  // (I believe Luoung et al. employ a more complex hueristic to classify partial formatting)

  // The font size, in points sizes smaller than the largest in document, or Small if
  // smaller than the majority size of the document
  fontSize: FontSize,

  isBold: Boolean,

  isItalic: Boolean,

  isBullet: Boolean,

  // Not entirely sure if word treats pictures and tables as paragraphs; these two
  // attributes may be unneccessary
  isPicture: Boolean,
  isTable: Boolean,

  // Does this paragraph have the same font size, family, bold, italic, and alignment as
  // the previous?

  isSameAsPrevious: Boolean
)


/**
  * Represents a font size as a relative size, with small sizes all represented as Small
  */
sealed trait FontSize
case object Small extends FontSize
final case class RelativeSize(size: Int) extends FontSize