package training

import features.Paragraph

/**
  * Describes the features used by the trainer, through a sequence of FeatureTemplate objects.
  */
object Features {
  val templates: Seq[FeatureTemplate[Paragraph]] = Seq(
    Unigram(_.location),
    Unigram(_.numberHint),
    Unigram(_.netHint),
    Unigram(_.length),
    Unigram(_.fontSize),
    Unigram(_.isBold),
    Unigram(_.isItalic),
    Unigram(_.isBullet),
    Bigram(_.isSameAsPrevious)
  )
}
