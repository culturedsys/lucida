package training

import features.Paragraph

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
}
case class Unigram[A](extractor: A => Any, relative: Int = 0) extends FeatureTemplate[A]
case class Bigram[A](extractor: A => Any, relative: Int = 0) extends FeatureTemplate[A]

object FeatureTemplate {
  /**
    * Convert a sequence of FeatureTemplate objects into an array of CRF++ feature template
    * strings. Assumes that the n'th template refers to the n'th tag, so will only work in simple
    * cases.
    */
  def templatesAsStrings[A](templates: Seq[FeatureTemplate[A]]): Array[String] = {
    templates.zipWithIndex.map {
      case (Unigram(_, relative), index) =>
        s"U$index:%x[$relative,$index]"
      case (Bigram(_, relative), index) =>
        s"B$index:%x[$relative,$index]"
    }.toArray
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


