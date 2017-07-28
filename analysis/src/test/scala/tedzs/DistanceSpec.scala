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

import org.scalatest.FunSpec

/**
  * Test of the edit distance calculation functions
  */
class DistanceSpec extends FunSpec {

  implicit val costs = TrivialCosts

  describe("distance") {
    it("should give a distance of 0 with identical one-element trees") {
      val tree = SimpleNode("a", Seq())
      assert(tree.distance(tree) == 0)
    }

    it("should give a distance of 0 with identical two-level trees") {
      val b = SimpleNode("b", Seq())
      val c = SimpleNode("c", Seq())
      val a = SimpleNode("a", Seq(b, c))

      assert(a.distance(a) == 0)
    }

    it("should give a distance of 1 to insert a leaf node") {
      val a1 = SimpleNode("a", Seq())
      val b = SimpleNode("b", Seq())
      val a2 = SimpleNode("a", Seq(b))

      assert(a1.distance(a2) == 1)
    }

    it("should give a distance of 1 to insert a sibling node") {
      val b = SimpleNode("b", Seq())
      val a1 = SimpleNode("a", Seq(b))
      val c = SimpleNode("c", Seq())
      val a2 = SimpleNode("a", Seq(b, c))

      assert(a1.distance(a2) == 1)
    }

    it("should give a distance of 1 to insert an intermediate node") {
      val c = SimpleNode("c", Seq())
      val a1 = SimpleNode("a", Seq(c))
      val b = SimpleNode("b", Seq(c))
      val a2 = SimpleNode("a", Seq(b))

      assert(a1.distance(a2) == 1)
    }

    it("should produce a correct distance for multi-level inserts") {
      val i = SimpleNode("i", Seq())
      val h = SimpleNode("h", Seq())
      val g = SimpleNode("g", Seq())
      val f = SimpleNode("f", Seq())
      val e = SimpleNode("e", Seq())

      val d = SimpleNode("d", Seq(h, i))
      val c1 = SimpleNode("c", Seq())
      val c2 = SimpleNode("c", Seq(f, g))
      val b1 = SimpleNode("b", Seq())
      val b2 = SimpleNode("b", Seq(e))

      val a1 = SimpleNode("a", Seq(b1, c1))
      val a2 = SimpleNode("a", Seq(b2, c2, d))

      assert(Node.distance(a1, a2) == 6)
    }
  }
}
