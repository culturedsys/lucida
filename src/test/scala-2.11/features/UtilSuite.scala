package features

import java.util.Comparator

import org.scalatest.FunSuite

/**
  * A suite of tests for miscellaneous utility functions used by the feature extractor.
  */
class UtilSuite extends FunSuite {

  test("groupedWhile on an empty sequence should be an empty sequence") {
    val from = Seq()
    val expected = Seq()
    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on a on-element sequence should be a one-element sequence with a one-element" +
    " sequence") {
    val from = Seq(1)
    val expected = Seq(Seq(1))
    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on a sequence with no elements should be a sequence of one-element " +
    "sequences") {
    val from = Seq(1, 2, 3, 4)
    val expected = Seq(Seq(1), Seq(2), Seq(3), Seq(4))
    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on a sequence with identical elements should be a one-element sequence of " +
    "those elements") {
    val from = Seq(1, 1, 1, 1)
    val expected = Seq(from)
    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on pairwise identical elements should be a sequence of pairs") {
    val from = Seq(1, 1, 2, 2, 3, 3, 4, 4)
    val expected = Seq(Seq(1, 1), Seq(2, 2), Seq(3, 3), Seq(4, 4))

    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on different-length equal seqments should collect all elements") {
    val from = Seq(1, 1, 1, 2, 2, 3, 3, 4, 4, 4)
    val expected = Seq(Seq(1, 1, 1), Seq(2, 2), Seq(3, 3), Seq(4, 4, 4))

    assert(Util.groupedWhile(from) === expected)
  }

  test("groupedWhile on separated equal segments should collect segments separately") {
    val from = Seq(1, 1, 1, 2, 1, 1)
    val expected = Seq(Seq(1, 1, 1), Seq(2), Seq(1, 1))

    assert(Util.groupedWhile(from) == expected)
  }

  test("groupedWhile with a custom equality function groups according to that function") {
    val from = Seq((1, 1), (1, 2), (1, 3), (2, 1), (1, 1), (1, 2))
    val expected = Seq(Seq((1, 1), (1, 2), (1, 3)), Seq((2, 1)), Seq((1, 1), (1, 2)))

    assert(Util.groupedWhile(from)(Ordering.by(_._1)) === expected)
  }

}
