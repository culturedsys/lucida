package training

import com.intel.imllib.crf.nlp.{CRF, CRFModel}
import model.{SectionHeader, SubsectionHeader, SubsubsectionHeader, Title}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * A driver application to train a model and save it to a file
  */
object Train extends TrainBase {
  def main(args: Array[String]) = {
    if (args.length < 1) {
      println("Supply path to training data")
      System.exit(1)
    }

    val trainingPath = args(0)

    if (args.length < 2) {
      println("Supply a path to write to model to")
      System.exit(1)
    }

    val outputPath = args(1)

    val conf = (new SparkConf).setAppName("Train")
    val sc = new SparkContext(conf)

    val labels = Seq(
      Title,
      SectionHeader,
      SubsectionHeader,
      SubsubsectionHeader
    )
    val trainingData = loadTrainingData(sc, trainingPath, labels)

    val templates = FeatureTemplate.templatesAsStrings(Features.templates, unqualifiedBigram=true)

    val model = CRF.train(templates, trainingData)

    CRFModel.saveBinaryFile(model, outputPath)
  }
}
