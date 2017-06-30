package features

/**
  * Miscellaneous utility methods used by the feature extractors
  */
object Util {
  def groupedWhile[A](from: Seq[A]): Seq[Seq[A]] = {
    def helper(acc: Vector[Seq[A]], seq: Seq[A]): Vector[Seq[A]] = seq match {
      case Seq() => acc
      case h +: _ =>
        val (equal, rest) = seq.span(_ == h)
        helper(acc :+ equal, rest)
    }

    helper(Vector(), from)
  }

}
