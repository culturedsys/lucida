package training

import com.intel.imllib.crf.nlp.{Sequence, Token}
import model._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
  * Shared functionality used by different training-based applications (i.e., the main model
  * builder, and the train and test program).
  */
trait TrainBase {
  /**
    * Load training data (in CRF++ format) from the files (one file for each sequence) in the
    * specified path
    *
    * @param sc the Spark context used for loading files.
    * @param path the directory containing the files of training data. This can be any kind of
    *             path that the `SparkContext` knows how to load, and must be accessible from all
    *             nodes.
    * @param labels a sequence of labels to use; if this is empty, any label in the training data
    *               is permitted.
    * @return an `RDD` pointing to `Sequence` objects representing each file in the training data
    *         directory.
    */
  def loadTrainingData(sc: SparkContext, path: String, labels: Seq[Tag] = Seq()): RDD[Sequence] = {
    val trainingFiles = sc.wholeTextFiles(path)

    trainingFiles.map { case (_, lines) =>
      val paragraphs = TrainingExtractor.extract(lines.split("\n"))
      val tags = paragraphs.map { p =>
        if (labels.isEmpty || labels.contains(p.tag))
          p.tag.toString
        else
          BodyText.toString
      }
      val tokens = FeatureTemplate.tokensAsStrings(Features.templates, paragraphs).zip(tags)
        .map{case (elements, tag) => Token.put(tag, elements)}.toArray
      Sequence(tokens)
    }

  }
}
