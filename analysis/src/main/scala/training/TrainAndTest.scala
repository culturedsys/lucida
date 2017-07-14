package training

import java.io.{File, FileNotFoundException, PrintStream}

import com.intel.imllib.crf.nlp.{CRF, Sequence, Token}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import cats.Semigroup.combine
import cats.instances.all._
import model.{SectionHeader, SubsectionHeader, SubsubsectionHeader, Title}

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

    val resultsPath = if (args.length < 2) {
      println("No results path specified, printing to standard output")
      None
    } else
      Some(args(1))

    val repeats = if (args.length < 3) {
      println("No repeats number supplied, assuming 1")
      1
    } else
      args(2).toInt

    val conf = (new SparkConf).setAppName("TrainAndTest")
    val sc = new SparkContext(conf)

    val allowedLabels = Seq(Title, SectionHeader, SubsectionHeader, SubsubsectionHeader)

    val trainingData = loadTrainingData(sc, trainingPath, allowedLabels)
    val labels = trainingData.flatMap(_.sequence.map(_.label)).distinct.collect.sorted

    // Divide the labelled data into two parts, using one to train a model and the rest to test
    // that model. Return counts for the number of true, predicted, and correctly predicted
    // tokens in each classification.
    def trainAndTest: (Map[String, Int], Map[String, Int], Map[String, Int]) = {
      val Array(train, test) = trainingData.randomSplit(Array(9, 1))
      val templates = FeatureTemplate.templatesAsStrings(Features.templates, unqualifiedBigram=true)
      val model = CRF.train(templates, train)
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

    val resultsStream: Option[PrintStream] = resultsPath match {
      case None => Some(System.out)
      case Some(path) => {
        try {
          Some(new PrintStream(path))
        } catch {
          case _: FileNotFoundException =>
            println(s"File $path cannot be opened for writing")
            None
        }
      }
    }

    resultsStream.foreach { rs =>
      val defaultMap = Map[String, Int]()

      // Run `trainAndTest` `repeats` times and aggregate the statistics
      val total =
        (0 until repeats)
          .foldLeft((defaultMap, defaultMap, defaultMap)) {
            (runningTotals, iteration) =>
              val results = trainAndTest
              rs.println(s"--$iteration")
              output(results, labels, rs)
              combine(runningTotals, results)
          }

      rs.println("--TOTAL")
      output(total, labels, rs)
    }
  }

  /**
    * Output the precision, recall, and F1 for each label in the supplied results set.
    *
    * @param results A tuple of maps, from label to true count, predicted count, and correctly
    *                predicted count, respectively
    * @param labels All labels to print results for
    * @param stream The stream to print output to
    */
  def output(results: (Map[String, Int], Map[String, Int], Map[String, Int]),
             labels: Seq[String],
             stream: PrintStream): Unit = {
    val (trueByCategory, predictedByCategory, correctByCategory) = results
    labels.foreach { label =>
      val nTrue = trueByCategory.getOrElse(label, 0)
      val nPredicted = predictedByCategory.getOrElse(label, 0)
      val nCorrect = correctByCategory.getOrElse(label, 0)

      if (nPredicted == 0 || nTrue == 0)
        stream.println(s"$label: No result")
      else {
        val precision = nCorrect.toDouble / nPredicted
        val recall = nCorrect.toDouble / nTrue
        val f1 = 2.0 * (precision * recall) / (precision + recall)

        stream.print(f"$label: P=$precision%1.2f R=$recall%1.2f F1=$f1%1.2f\n")
      }
    }
  }

}
