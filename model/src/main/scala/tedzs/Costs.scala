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
  * A trait encapsulating a set of insert, delete, and change costs for `tedzs.Node`s of type `A`
  */
trait Costs[-A] {
  def insert(node: Node[A]): Int
  def delete(node: Node[A]): Int
  def change(from: Node[A], to: Node[A]): Int
}

/**
  * Implicit costs instances for common label types.
  */
object Costs {
  implicit object IntCosts extends Costs[Int] {
    def insert(node: Node[Int]): Int = node.label
    def delete(node: Node[Int]): Int = node.label
    def change(from: Node[Int], to: Node[Int]): Int = math.abs(from.label - to.label)
  }
}

/**
  * Trivial cost metric where the cost is 1 for all operations, regardless of label.
  */
object TrivialCosts extends Costs[Any] {
  def insert(node: Node[Any]): Int = 1
  def delete(node: Node[Any]): Int = 1
  def change(from: Node[Any], to: Node[Any]): Int = if (from.label == to.label) 0 else 1
}
