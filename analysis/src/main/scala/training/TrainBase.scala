package training

import com.intel.imllib.crf.nlp.{Sequence, Token}
import features.TrainingExtractor
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * Shared functionality used by different training-based applications (i.e., the main model
  * builder, and the train and test program).
  */
trait TrainBase {
  def loadTrainingDate(sc: SparkContext, path: String): RDD[Sequence] = {
    val trainingFiles = sc.wholeTextFiles(path)

    trainingFiles.map { case (_, lines) =>
      val paragraphs = TrainingExtractor.extract(lines.split("\n"))
      val tags = paragraphs.map(_.tag.get.toString)
      val tokens = FeatureTemplate.tokensAsStrings(Features.templates, paragraphs).zip(tags)
        .map{case (elements, tag) => Token.put(tag, elements)}.toArray
      Sequence(tokens)
    }

  }
}
