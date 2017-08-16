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
  * A trait encapsulating the different edit operations - inserting, deleting, or changing a node.
  */
sealed trait Edit[+A]

final case class Insert[A](node: Node[A]) extends Edit[A] {
  override  def toString = s"Insert(${node.label})"
}

final case class Delete[A](node: Node[A]) extends Edit[A] {
  override def toString: String = s"Delete(${node.label}"
}

final case class Change[A](from: Node[A], to: Node[A]) extends Edit[A] {
  override def toString: String = s"Change(${from.label}, ${to.label})"
}