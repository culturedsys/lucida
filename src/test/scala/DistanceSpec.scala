import org.scalatest.FunSpec

/**
  * Test of the edit distance calculation functions
  */
class DistanceSpec extends FunSpec {

  implicit val costs = Costs.TrivialCosts

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
  }
}
