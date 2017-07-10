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
    var correct = 0
    var tested = 0

    for (_ <- 0 until repeats) {
      val Array(train, test) = trainingData.randomSplit(Array(9, 1))
      val model = CRF.train(FeatureTemplate.templatesAsStrings(Features.templates), train)
      val prediction = model.predict(test)
      tested += test.map(_.toArray.length).reduce(_ + _)
      correct += test.zip(prediction).map { case (ts, ps) => ts.compare(ps) }.reduce(_ + _)
    }

    println("Correctly predicted tags: " + correct + " / " + tested)
  }

}
