package training

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * Additional functions on RDDs that are useful in training and testing
  */
class RDDFunctions[T](self: RDD[T]) {
  /**
    * Split the given RDD into two, as if it had been split into `subsamples` subsamples, and the
    * `index`-th subsample was extracted. Returns a pair of `RDD`s, the first of which is the
    * extracted subsample, and the second of which is the remainder of the data.
    */
  def extract(subsamples: Int, index: Int)(implicit evidence: ClassTag[T]): (RDD[T], RDD[T]) = {
    val size = self.count / subsamples
    val subsample = self.zipWithIndex.filter(_._2 / size == index).map(_._1)
    val rest = self.zipWithIndex.filter(_._2 / size != index).map(_._1)
    (subsample, rest)
  }
}

object RDDFunctions {
  implicit def fromRDD[T](self: RDD[T]): RDDFunctions[T] = new RDDFunctions(self)
}