package tedzs

import org.scalatest.FunSpec

/**
  * Tests for general utilities related to manipulating and traversing Nodes
  */
class NodeSpec extends FunSpec {
  describe("A node") {
    describe("postOrder") {
      it("should return just the root for a tree with no children") {
        val tree = Node("1", Seq())
        assert(tree.postOrder == Vector(tree))
      }

      it("should return children before parents for a two-level tree") {
        val b = Node("b", Seq())
        val c = Node("c", Seq())
        val a = Node("a", Seq(b, c))
        assert(a.postOrder.map(_.label) == Vector("b", "c", "a"))
      }

      it("should return post-order for a multi-level tree") {
        val d = Node("d", Seq())
        val e = Node("e", Seq())
        val f = Node("f", Seq())
        val g = Node("g", Seq())
        val b = Node("b", Seq(d, e))
        val c = Node("c", Seq(f, g))
        val a = Node("a", Seq(b, c))

        assert(a.postOrder.map(_.label) == Vector("d", "e", "b", "f", "g", "c", "a"))
      }
    }

    describe("postOrderPaths") {
      it("should return a single element path for a single-element tree") {
        val tree = Node("a", Seq())
        assert(tree.postOrderPaths == Vector(Seq(tree)))
      }

      it("should return a one-element path for child nodes in a two-level tree") {
        val b = Node("b", Seq())
        val c = Node("c", Seq())
        val a = Node("a", Seq(b, c))

        assert(a.postOrderPaths.map(_.tail) == Vector(Seq(a), Seq(a), Seq()))
      }

      it("should return two-element paths for child nodes in a three-level tree") {
        val c = Node("c", Seq())
        val b = Node("b", Seq(c))
        val a = Node("a", Seq(b))

        assert(a.postOrderPaths.map(_.map(_.label)) ==
          Vector(Seq("c", "b", "a"), Seq("b", "a"), Seq("a")))
      }
    }

    describe("leftMostDescendants") {
      it("should return the root for a one-element tree") {
        val tree = Node("a", Seq())

        assert(tree.leftMostDescendants == Vector(0))
      }

      it("should return the left-most node for a two-level tree") {
        val b = Node("b", Seq())
        val c = Node("c", Seq())
        val a = Node("a", Seq(b, c))

        assert(a.leftMostDescendants == Vector(0, 1, 0))
      }

      it("should return the left-most node for a three-level tree") {
        val c = Node("c", Seq())
        val b = Node("b", Seq(c))
        val a = Node("a", Seq(b))

        assert(a.leftMostDescendants == Vector(0, 0, 0))
      }
    }

    describe("keyroots") {
      it("should return the root for a one-node tree") {
        val tree = Node("a", Seq())

        assert(tree.keyroots == Vector(0))
      }

      it("should return the root and the right siblings for a two-level tree") {
        val b = Node("b", Seq())
        val c = Node("c", Seq())
        val a = Node("a", Seq(b, c))

        assert(a.keyroots == Seq(1, 2))
      }

      it("should return all the right siblings in a two-level tree") {
        val b = Node("b", Seq())
        val c = Node("c", Seq())
        val d = Node("d", Seq())
        val a = Node("a", Seq(b, c, d))

        assert(a.keyroots == Seq(1, 2, 3))
      }

      it("should return all the right siblings in a three-level tree") {
        val tree = Node("a", Seq(
          Node("b", Seq(
            Node("e", Seq()),
            Node("f", Seq())
          )),
          Node("c", Seq(Node("g", Seq()))),
          Node("d", Seq())
        ))

        assert(tree.keyroots == Seq(1, 4, 5, 6))
      }
    }
  }
}
