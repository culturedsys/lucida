package model
import tedzs.Node

/**
  * Represents the structure of a document, as a tree of Paragraphs
  */
case class Structure[+A](
  content: A,
  children: Seq[Structure[A]] = Seq()
) extends Node[A] {

  def label = content

  def map[B](f: A => B): Structure[B] = {
      Structure(f(content), children.map(_.map(f)))
    }
}

object Structure {
  val TITLE = 0
  val SECTION = 1
  val SUBSECTION = 2
  val SUBSUBSECTION = 3
  val BODY = 10

  /**
    * Converts a sequence of `Paragraph`, `Tag` pairs into a sequence of `Structure`s representing
    * the structure implied by the heading, subheading, etc., information in the tags.
    *
    * @return a sequence of top-level `Structure`s
    */
  def fromParagraphs(paragraphs: Seq[TaggedParagraph]): Seq[Structure[TaggedParagraph]] = {
    def buildSubTree(acc: Seq[Structure[TaggedParagraph]],
                     level: Int, paragraphs: Seq[TaggedParagraph]):
        (Seq[Structure[TaggedParagraph]], Seq[TaggedParagraph]) = paragraphs match {
      case Seq() => (acc, Seq())
      case para +: t =>
        val subLevel = tagLevel(para.tag)
        if (subLevel > level) {
          val (thisNode, rest) =
            if (subLevel == BODY) {
              (Structure(para), t)
            } else {
              val (children, rest) = buildSubTree(Seq(), subLevel, t)
              (Structure(para, children), rest)
            }
          buildSubTree(acc :+ thisNode, level, rest)
        } else
          (acc, para +: t)
    }
    buildSubTree(Seq(), -1, paragraphs)._1
  }

  def tagLevel: (Tag => Int) = {
    case Title => TITLE
    case SectionHeader => SECTION
    case SubsectionHeader => SUBSECTION
    case SubsubsectionHeader => SUBSUBSECTION
    case _ => BODY
  }
}
