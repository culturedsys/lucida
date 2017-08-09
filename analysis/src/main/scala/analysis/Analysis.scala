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
      (Structure[(String, Change)], Structure[(String, Change)]) = {

    implicit val costs = StringCosts

    val fromForest = Structure.fromParagraphs(classify(from, model)).map(_.map(_.description))
    val fromStructure = Structure("From", fromForest)

    val toForest = Structure.fromParagraphs(classify(to, model)).map(_.map(_.description))
    val toStructure = Structure("To", toForest)

    val operations = Node.mapping(fromStructure, toStructure)

    def fromChange(fromText: String): Edit[String] => Option[Change] = {
      case Delete(Structure(text, _)) if text == fromText => Some(Deleted)
      case Change(Structure(text, _), _) if text == fromText => Some(Changed)
      case _ => None
    }

    val fromChanges = fromStructure.map { text =>
      val change = operations.flatMap(fromChange(text)(_)).headOption.getOrElse(Unchanged)
      (text, change)
    }

    def toChange(toText: String): Edit[String] => Option[Change] = {
      case Insert(Structure(text, _)) if text == toText => Some(Inserted)
      case Change(_, Structure(text, _)) if text == toText => Some(Changed)
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
  * Calculate the costs of edits on nodes of strings, which are the string edit distance of the
  * change between strings.
  */
object StringCosts extends Costs[String] {
  override def insert(node: Node[String]): Int = node.label.length

  override def delete(node: Node[String]): Int = node.label.length

  override def change(from: Node[String], to: Node[String]): Int = {
    val costs = Array.ofDim[Int](from.label.length + 1, to.label.length + 1)

    costs(0)(0) = 0

    for (i <- 1 to from.label.length) {
      costs(i)(0) = i
    }

    for (j <- 1 to to.label.length) {
      costs(0)(j) = j
    }

    for {
      i <- 1 to from.label.length
      j <- 1 to to.label.length
    } {
      val insertCost = costs(i)(j - 1) + 1
      val deleteCost = costs(i - 1)(j) + 1
      val changeCost = costs(i - 1)(j - 1) +
        (if (from.label.charAt(i - 1) == to.label.charAt(j - 1)) 0 else 1)

      costs(i)(j) = math.min(math.min(insertCost, deleteCost), changeCost)
    }

    costs(from.label.length)(to.label.length)
  }
}


