package model

/**
  * Miscellaneous utility methods used by the feature extractors
  */
object Util {
  def groupedWhile[A](from: Seq[A])(implicit equiv: Equiv[A]):
  Seq[Seq[A]] = {
    def helper(acc: Vector[Seq[A]], seq: Seq[A]): Vector[Seq[A]] = seq match {
      case Seq() => acc
      case h +: t =>
        val (equal, rest) = t.span(equiv.equiv(h, _))
        helper(acc :+ (h +: equal), rest)
    }

    helper(Vector(), from)
  }

}
