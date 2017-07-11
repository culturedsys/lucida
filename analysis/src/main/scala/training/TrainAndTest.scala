package training

import com.intel.imllib.crf.nlp.CRF
import org.apache.spark.{SparkConf, SparkContext}

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

    val repeats = if (args.length < 2) 1 else args(1).toInt

    val conf = (new SparkConf).setAppName("TrainAndTest")
    val sc = new SparkContext(conf)

    val trainingData = loadTrainingDate(sc, trainingPath)
    val labels = trainingData.flatMap(_.sequence.map(_.label)).distinct.collect.sorted

    //TODO: Tidy this up by breaking functionality up into named methods
    val statistics = (0 until repeats).flatMap { _ =>
      val Array(train, test) = trainingData.randomSplit(Array(9, 1))
      val model = CRF.train(FeatureTemplate.templatesAsStrings(Features.templates), train)
      val prediction = model.predict(test)

      val trueByCategory = test.flatMap(_.sequence)
                                .groupBy(_.label)
                                .mapValues(_.size).collect
      val predictedByCategory = prediction.flatMap(_.sequence)
                                          .groupBy(_.label).mapValues(_.size)
                                          .collect
      val correctByCategory = test.zip(prediction).flatMap {
        case (t, p) => t.sequence.zip(p.sequence)
      }.filter {
        case (t, p) => t.label == p.label
      }.groupBy(_._1.label).mapValues(_.size).collect

      def lookup(collection: Seq[(String, Int)], label: String): Int =
        collection.find(_._1 == label).map(_._2).getOrElse(0)

      labels.map { label =>
        val nTrue = lookup(trueByCategory, label)
        val nPredicted = lookup(predictedByCategory, label)
        val nCorrect = lookup(correctByCategory, label)

        (label, nTrue, nPredicted, nCorrect)
      }
    }.foldLeft(Map[String, (Int, Int, Int)]().withDefaultValue((0, 0, 0))) { (map, data) =>
      val (label, nTrue, nPredicted, nCorrect) = data
      val (oldTrue, oldPredicted, oldCorrect) = map(label)
      map + (label -> (oldTrue + nTrue, oldPredicted + nPredicted, oldCorrect + nCorrect))
    }

    labels.foreach { label =>
      val (nTrue, nPredicted, nCorrect) = statistics(label)
      val precision = if (nPredicted == 0) nPredicted else nCorrect.toDouble / nPredicted
      val recall = nCorrect.toDouble / nTrue

      printf("%s: P=%1.2f R=%1.2f F1=%1.2f\n",
        label,
        precision,
        recall,
        2.0 * (precision * recall) / (precision + recall))
    }
  }
}
