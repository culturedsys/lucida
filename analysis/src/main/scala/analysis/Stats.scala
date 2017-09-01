package analysis

import java.lang.management.{ManagementFactory, MemoryType}

import scala.collection.JavaConverters._

/**
  * Record statistics (execution time, memory usage) about the execution of a piece of code.
  *
  *
  */
case class Stats[A](value: A, time: Long, maxHeap: Long) {
  /**
    * Print out the statistics contained in this object.
    */
  def report(): Unit = {
    Console.err.println(s"Time taken: $time ms")
    Console.err.println(s"Max heap usage: $maxHeap bytes")
  }
}

object Stats {
  /**
    * Execute the supplied block of code, and return its results wrapped in a Stats object
    * containing information about
    */
  def withStats[A](block: => A): Stats[A] = {
    val start = System.currentTimeMillis()
    val value = block
    val end = System.currentTimeMillis()

    val maxHeap = ManagementFactory.getMemoryPoolMXBeans.asScala
                        .filter(_.getType == MemoryType.HEAP)
                        .map(_.getPeakUsage.getUsed)
                        .sum

    Stats(value, end - start, maxHeap)
  }
}
