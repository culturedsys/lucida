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
  * Test the functions for generating mappings between two trees
  */
class MappingSpec extends FunSpec {
  implicit val costs =  TrivialCosts

  describe("mapping") {
    it("should produce an empty mapping for two identical nodes") {
      val tree = SimpleNode("a", Seq())
      assert(Node.mapping(tree, tree) == Seq())
    }

    val b = SimpleNode("b", Seq())
    val a1 = SimpleNode("a", Seq())
    val a2 = SimpleNode("a", Seq(b))

    it("should produce an insert mapping for adding one node") {
      assert(Node.mapping(a1, a2) == Seq(Insert(b)))
    }

    it("should produce a delete mapping for deleting one node") {
      assert(Node.mapping(a2, a1) == Seq(Delete(b)))
    }

    it("should produce a change mapping for relabelling a single node") {
      assert(Node.mapping(a1, b) == Seq(Change(a1, b)))
    }

    it("should produce a number of inserts for inserting a number of nodes") {
      val a1 = SimpleNode("a", Seq())
      val d = SimpleNode("d", Seq())
      val c = SimpleNode("c", Seq(d))
      val b = SimpleNode("b", Seq(c))
      val a2 = SimpleNode("a", Seq(b))

      assert(Node.mapping(a1, a2) == Seq(Insert(d), Insert(c), Insert(b)))
    }

    it("should produce a correct mapping for multi-level inserts") {
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

      assert(Node.mapping(a1, a2).toSet ==
        Set(Insert(i), Insert(h), Insert(d),
          Insert(g), Insert(f),
          Insert(e)))
    }

    it("should give correct mappings for combined insertions and deletions") {
      val e = SimpleNode("e", Seq())
      val d1 = SimpleNode("d", Seq())
      val d2 = SimpleNode("d", Seq(e))
      val c = SimpleNode("c", Seq(d1))
      val b = SimpleNode("b", Seq())
      val a1 = SimpleNode("a", Seq(b, c))
      val a2 = SimpleNode("a", Seq(b, d2))

      assert(Node.mapping(a1, a2).toSet == Set(Delete(c), Insert(e)))
    }
  }
}
