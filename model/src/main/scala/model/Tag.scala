package model

/**
  * The different tags assigned to paragraphs by the model.
  */
sealed trait Tag {
  this: Product =>

  /**
    * Luong et al. name their tags with an initial lower-case letter, but it is usual to name
    * case objects with an initial upper-case letter; this toString method maps between the two.
    */
  override lazy val toString: String = {
    val name = productPrefix
    name.charAt(0).toLower + name.substring(1)
  }
}

case object Address extends Tag
case object Affiliation extends Tag
case object Author extends Tag
case object BodyText extends Tag
case object Category extends Tag
case object Construct extends Tag
case object Copyright extends Tag
case object Email extends Tag
case object Equation extends Tag
case object Figure extends Tag
case object FigureCaption extends Tag
case object Footnote extends Tag
case object Keyword extends Tag
case object ListItem extends Tag
case object Note extends Tag
case object Page extends Tag
case object Reference extends Tag
case object SectionHeader extends Tag
case object SubsectionHeader extends Tag
case object SubsubsectionHeader extends Tag
case object Table extends Tag
case object TableCaption extends Tag
case object Title extends Tag

object Tag {
  /**
    * return a Tag object corresponding to the name supplied.
    */
  def fromString: PartialFunction[String, Tag] = {
    case "address" => Address
    case "affiliation" => Affiliation
    case "author" => Author
    case "bodyText" => BodyText
    case "category" => Category
    case "construct" => Construct
    case "copyright" => Copyright
    case "email" => Email
    case "equation" => Equation
    case "figure" => Figure
    case "figureCaption" => FigureCaption
    case "footnote" => Footnote
    case "keyword" => Keyword
    case "listItem" => ListItem
    case "note" => Note
    case "page" => Page
    case "reference" => Reference
    case "sectionHeader" => SectionHeader
    case "subsectionHeader" => SubsectionHeader
    case "subsubsectionHeader" => SubsubsectionHeader
    case "table" => Table
    case "tableCaption" => TableCaption
    case "title" => Title
  }
}

