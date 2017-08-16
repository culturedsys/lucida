package model

/**
  * Describes the features used by the trainer, through a sequence of FeatureTemplate objects.
  */
object Features {
  val templates = Seq[FeatureTemplate[Paragraph]](
    Unigram(_.location),
    Unigram(_.numberHint),
    Unigram(_.netHint),
    Unigram(_.length),
    Unigram(_.fontSize),
    Unigram(_.isBold),
    Unigram(_.isItalic),
    Unigram(_.isBullet),
    Bigram(_.isSameAsPrevious)
  ) ++ (0 until Paragraph.WORDS).flatMap { index =>
    Seq[FeatureTemplate[Paragraph]](
      Unigram(_.words(index).word),
      Unigram(_.words(index).lowerCase),
      Unigram(_.words(index).unpunctuated),
      Unigram(_.words(index).wordCase),
      Unigram(_.words(index).digits)
    )
  }
}
