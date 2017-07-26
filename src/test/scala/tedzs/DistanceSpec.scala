package tedzs

import org.scalatest.FunSpec

/**
  * Test of the edit distance calculation functions
  */
class DistanceSpec extends FunSpec {

  implicit val costs = TrivialCosts

  describe("distance") {
    it("should give a distance of 0 with identical one-element trees") {
      val tree = Node("a", Seq())
      assert(tree.distance(tree) == 0)
    }

    it("should give a distance of 0 with identical two-level trees") {
      val b = Node("b", Seq())
      val c = Node("c", Seq())
      val a = Node("a", Seq(b, c))

      assert(a.distance(a) == 0)
    }

    it("should give a distance of 1 to insert a leaf node") {
      val a1 = Node("a", Seq())
      val b = Node("b", Seq())
      val a2 = Node("a", Seq(b))

      assert(a1.distance(a2) == 1)
    }

    it("should give a distance of 1 to insert a sibling node") {
      val b = Node("b", Seq())
      val a1 = Node("a", Seq(b))
      val c = Node("c", Seq())
      val a2 = Node("a", Seq(b, c))

      assert(a1.distance(a2) == 1)
    }

    it("should give a distance of 1 to insert an intermediate node") {
      val c = Node("c", Seq())
      val a1 = Node("a", Seq(c))
      val b = Node("b", Seq(c))
      val a2 = Node("a", Seq(b))

      assert(a1.distance(a2) == 1)
    }

    it("should produce a correct distance for multi-level inserts") {
      val i = Node("i", Seq())
      val h = Node("h", Seq())
      val g = Node("g", Seq())
      val f = Node("f", Seq())
      val e = Node("e", Seq())

      val d = Node("d", Seq(h, i))
      val c1 = Node("c", Seq())
      val c2 = Node("c", Seq(f, g))
      val b1 = Node("b", Seq())
      val b2 = Node("b", Seq(e))

      val a1 = Node("a", Seq(b1, c1))
      val a2 = Node("a", Seq(b2, c2, d))

      assert(Node.distance(a1, a2) == 6)
    }
  }
}
