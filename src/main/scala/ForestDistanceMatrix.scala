/**
  * A class to store matrixes of distances between two forests. This is just a two-dimensional
  * array with unusual indexing. When created, ranges (which must be positive) are specified for
  * rows and columns, and any values in these ranges can be used as indexes. In addition,
  * negative values can be used to access an additional row and column (this is used to represent
  * the empty forest).
  */
case class ForestDistanceMatrix(rows: Range, cols: Range) {

  assert(rows.start >= 0 && cols.start >= 0)

  private val array = Array.ofDim[Int](rows.size + 1, cols.size + 1)

  val rowBase: Int = rows.start
  val colBase: Int = cols.start


  def apply(row: Int, col: Int): Int =
    array(row - rowBase + 1)(col - colBase + 1)

  def update(row: Int, col: Int, value: Int): Unit =
    array(row - rowBase + 1)(col - colBase + 1) = value
}
