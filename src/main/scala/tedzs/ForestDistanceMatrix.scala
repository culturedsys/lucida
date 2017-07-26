/*
 * Copyright 2017 Tim Fisken
 *
 * This file is part of ted-zs-scala.
 *
 * ted-zs-scala is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ted-zs-scala is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with ted-zs-scala.  If
 * not, see <http://www.gnu.org/licenses/>
 */

package tedzs

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
