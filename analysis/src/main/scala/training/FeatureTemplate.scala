package training

import model.Paragraph

/**
  * Represents a feature template, either unigram or bigram.
  *
  * Type parameter A specifies the type of the data structure storing data to be used to construct
  *
  */
trait FeatureTemplate[A] {
  /**
    * Extracts the attribute that is relevant to this feature from an object of type A.
    */
  def extractor: A => Any

  /**
    * Denotes the relative index of the token which we should consider when assigning the feature
    * to the current token.
    */
  def relative: Int

  /**
    * Returns a tag representing the type of template (`U` for unigrams, `B` for bigrams)
    */
  def tag: String = this match {
    case Unigram(_, _) => "U"
    case Bigram(_, _) => "B"
  }
}
case class Unigram[A](extractor: A => Any, relative: Int = 0) extends FeatureTemplate[A]
case class Bigram[A](extractor: A => Any, relative: Int = 0) extends FeatureTemplate[A]

object FeatureTemplate {
  /**
    * Convert a sequence of FeatureTemplate objects into an array of CRF++ feature template
    * strings. Assumes that the n'th template refers to the n'th tag, so will only work in simple
    * cases.
    *
    * @param templates a sequence of feature template specifications
    * @param unqualifiedBigram if true, add a template for a bigram with no dependence on any
    *                          particular attribute
    */
  def templatesAsStrings[A](templates: Seq[FeatureTemplate[A]],
                            unqualifiedBigram: Boolean = false): Array[String] = {

    val templateStrings = templates.zipWithIndex.map {
      case (template, index) =>
        s"${template.tag}$index:%x[${template.relative},$index]"
    }
    (if (unqualifiedBigram)
      templateStrings :+ "B"
    else
      templateStrings).toArray
  }

  /**
    * Convert a sequence of tokens to an sequence of arrays of strings, where each array contains
    * the string representations of the tags of the given token, in the order specified by the
    * templates.
    */
  def tokensAsStrings[A](templates: Seq[FeatureTemplate[A]], tokens: Seq[A]):
  Seq[Array[String]] = {
    tokens.map { token =>
      templates.map { template =>
          template.extractor(token).toString
      }.toArray
    }
  }
}


