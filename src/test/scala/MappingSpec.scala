import org.scalatest.FunSpec

/**
  * Test the functions for generating mappings between two trees
  */
class MappingSpec extends FunSpec {
  implicit val costs =  Costs.TrivialCosts

  describe("mapping") {
    it("should produce an empty mapping for two identical nodes") {
      val tree = Node("a", Seq())
      assert(Node.mapping(tree, tree) == Seq())
    }

    val b = Node("b", Seq())
    val a1 = Node("a", Seq())
    val a2 = Node("a", Seq(b))

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
      val a1 = Node("a", Seq())
      val d = Node("d", Seq())
      val c = Node("c", Seq(d))
      val b = Node("b", Seq(c))
      val a2 = Node("a", Seq(b))

      assert(Node.mapping(a1, a2) == Seq(Insert(d), Insert(c), Insert(b)))
    }

    it("should produce a correct mapping for multi-level inserts") {
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

      assert(Node.mapping(a1, a2).toSet ==
        Set(Insert(i), Insert(h), Insert(d),
          Insert(g), Insert(f),
          Insert(e)))
    }

    it("should give correct mappings for combined insertions and deletions") {
      val e = Node("e", Seq())
      val d1 = Node("d", Seq())
      val d2 = Node("d", Seq(e))
      val c = Node("c", Seq(d1))
      val b = Node("b", Seq())
      val a1 = Node("a", Seq(b, c))
      val a2 = Node("a", Seq(b, d2))

      assert(Node.mapping(a1, a2).toSet == Set(Delete(c), Insert(e)))
    }
  }
}
