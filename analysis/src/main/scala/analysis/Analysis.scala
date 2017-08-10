package analysis

import com.intel.imllib.crf.nlp.{CRFModel, Sequence, Token}
import model.{Paragraph, Structure, Tag, TaggedParagraph}
import tedzs._
import training.{FeatureTemplate, Features}

/**
  * Functions for analysing and comparing documents.
  */
object Analysis {
  /**
    * Assign tags to the paragraphs in a sequence, according to their semantic classification as
    * predicted by the supplied CRFModel.
    */
  def classify(doc: Seq[Paragraph], model: CRFModel): Seq[TaggedParagraph] = {
    val tokens = Sequence(FeatureTemplate.tokensAsStrings(Features.templates, doc)
                            .map(Token.put).toArray)
    val Array(tags) = model.predict(Array(tokens))
    doc.zip(tags.sequence).map {
      case (para, token) => TaggedParagraph.addTag(para, Tag.fromString(token.label))
    }
  }

  /**
    * Compare the structure of two documents, represented as sequences of paragraphs.
    *
    * @return a pair of Structures, where each element of the structure is a pair consisting of a
    *         string (the text of the paragraph) and a Change instance indicating how, if at all,
    *         that paragraph has changed between the two documents.
    */
  def compare(from: Seq[Paragraph], to: Seq[Paragraph], model: CRFModel):
      (Structure[(Paragraph, Change)], Structure[(Paragraph, Change)]) = {

    implicit val costs = StringCosts

    val fromClassified = classify(from, model)
    val fromStructure = Structure(fromClassified.head,
                                  Structure.fromParagraphs(fromClassified.tail))

    val toClassified = classify(to, model)
    val toStructure = Structure(toClassified.head,
      Structure.fromParagraphs(toClassified.tail))

    val operations = Node.mapping(fromStructure, toStructure)

    def fromChange(fromPara: Paragraph): Edit[Paragraph] => Option[Change] = {
      case Delete(Structure(para, _)) if para == fromPara => Some(Deleted)
      case Change(Structure(para, _), _) if para == fromPara => Some(Changed)
      case _ => None
    }

    val fromChanges = fromStructure.map { text =>
      val change = operations.flatMap(fromChange(text)(_)).headOption.getOrElse(Unchanged)
      (text, change)
    }

    def toChange(toPara: Paragraph): Edit[Paragraph] => Option[Change] = {
      case Insert(Structure(para, _)) if para == toPara => Some(Inserted)
      case Change(_, Structure(para, _)) if para == toPara => Some(Changed)
      case _ => None
    }

    val toChanges = toStructure.map { text =>
      val change = operations.flatMap(toChange(text)(_)).headOption.getOrElse(Unchanged)
      (text, change)
    }

    (fromChanges, toChanges)
  }
}

/**
  * Represents the different possible changes to an element in the document structure.
  */
sealed trait Change
case object Inserted extends Change
case object Deleted extends Change
case object Changed extends Change
case object Unchanged extends Change

/**
  * Calculate the costs of edits on nodes of paragraphs, which are the string edit distance of the
  * change between strings making up the paragraphs.
  */
object StringCosts extends Costs[Paragraph] {
  override def insert(node: Node[Paragraph]): Int = node.label.description.length

  override def delete(node: Node[Paragraph]): Int = node.label.description.length

  override def change(from: Node[Paragraph], to: Node[Paragraph]): Int = {
    val fromString = from.label.description
    val toString = to.label.description

    val costs = Array.ofDim[Int](fromString.length + 1, toString.length + 1)

    costs(0)(0) = 0

    for (i <- 1 to fromString.length) {
      costs(i)(0) = i
    }

    for (j <- 1 to toString.length) {
      costs(0)(j) = j
    }

    for {
      i <- 1 to fromString.length
      j <- 1 to toString.length
    } {
      val insertCost = costs(i)(j - 1) + 1
      val deleteCost = costs(i - 1)(j) + 1
      val changeCost = costs(i - 1)(j - 1) +
        (if (fromString.charAt(i - 1) == toString.charAt(j - 1)) 0 else 1)

      costs(i)(j) = math.min(math.min(insertCost, deleteCost), changeCost)
    }

    costs(from.label.length)(to.label.length)
  }
}


