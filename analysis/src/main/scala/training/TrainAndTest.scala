package training

import com.intel.imllib.crf.nlp.{CRF, Token}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import cats.Semigroup.combine
import cats.instances.all._

/**
  * Train a model, keeping some tagged data separate to use as test data; report statistics based
  * on this test data.
  */
object TrainAndTest extends TrainBase {
  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      println("Supply path to training data")
      System.exit(1)
    }

    val trainingPath = args(0)

    val repeats = if (args.length < 2) {
      println("No repeats number supplied, assuming 1")
      1
    } else
      args(1).toInt

    val conf = (new SparkConf).setAppName("TrainAndTest")
    val sc = new SparkContext(conf)

    val trainingData = loadTrainingDate(sc, trainingPath)
    val labels = trainingData.flatMap(_.sequence.map(_.label)).distinct.collect.sorted

    // Divide the labelled data into two parts, using one to train a model and the rest to test
    // that model. Return counts for the number of true, predicted, and correctly predicted
    // tokens in each classification.
    def trainAndTest: (Map[String, Int], Map[String, Int], Map[String, Int]) = {
      val Array(train, test) = trainingData.randomSplit(Array(9, 1))
      val model = CRF.train(FeatureTemplate.templatesAsStrings(Features.templates), train)
      val prediction = model.predict(test)

      // Tokens with their true labels attached
      val trueTokens = test.flatMap(_.sequence)

      // Tokens with their predicted labels attached
      val predictedTokens = prediction.flatMap(_.sequence)

      // Those tokens where the predicted label is correct, i.e., the same as the true label
      val correctTokens = trueTokens.zip(predictedTokens).filter {
        case (t, p) => t.label == p.label
      }.map(_._1)

      // Return a map from labels to how many elements in the collection of tokens have that label
      def sizeByCategory(tokens: RDD[Token]): Map[String, Int] =
        tokens.groupBy(_.label).mapValues(_.size).collect.toMap

      (sizeByCategory(trueTokens), sizeByCategory(predictedTokens), sizeByCategory(correctTokens))
    }

    val defaultMap = Map[String, Int]()

    // Run `trainAndTest` `repeats` times and aggregate the statistics
    val (trueByCategory, predictedByCategory, correctByCategory) =
      (0 until repeats)
        .foldLeft((defaultMap, defaultMap, defaultMap)) {
          (runningTotals, _) =>
            combine(runningTotals, trainAndTest)
        }

    labels.foreach { label =>
      val nTrue = trueByCategory.getOrElse(label, 0)
      val nPredicted = predictedByCategory.getOrElse(label, 0)
      val nCorrect = correctByCategory.getOrElse(label, 0)

      if (nPredicted == 0 || nTrue == 0)
        println(s"$label: No result")
      else {
        val precision = nCorrect.toDouble / nPredicted
        val recall = nCorrect.toDouble / nTrue

        printf("%s: P=%1.2f R=%1.2f F1=%1.2f\n",
          label,
          precision,
          recall,
          2.0 * (precision * recall) / (precision + recall))
      }
    }
  }
}
