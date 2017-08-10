package analysis

import model.{Common, NetOther, NumberOther, Paragraph}
import org.scalatest.FunSuite
import tedzs.Node

/**
  * Test the cost functions for edits
  */
class CostsSuite extends FunSuite {
  case class TestNode(labelString: String) extends Node[Paragraph] {
    val label = Paragraph(labelString, Seq(), 0, NumberOther, NetOther, 0, Common, false, false, false,
      false)

    val children = Seq()
  }

  test("Cost of inserting an empty string should be 0") {
    assert(StringCosts.insert(TestNode("")) == 0)
  }

  test("Cost of inserting a string should be equal to its length") {
    assert(StringCosts.insert(TestNode("four")) == 4)
  }

  test("Cost of deleting an empty string should be 0") {
    assert(StringCosts.delete(TestNode("")) == 0)
  }

  test("Cost of deleting a string should be equal to its length") {
    assert(StringCosts.delete(TestNode("four")) == 4)
  }

  test("Cost of changing an empty string to an empty string should be 0") {
    assert(StringCosts.change(TestNode(""), TestNode("")) == 0)
  }

  test("Cost of changing a string to a longer string should be difference in length") {
    assert(StringCosts.change(TestNode("four"), TestNode("fourteen")) == 4)
  }

  test("Cost of changing a string to a shorter string should be difference in length") {
    assert(StringCosts.change(TestNode("fourteen"), TestNode("four")) == 4)
  }

  test("Cost of changing characters in a string should be number of characters to change") {
    assert(StringCosts.change(TestNode("four"), TestNode("fair")) == 2)
  }

  test("Cost of changing and adding characters should be the same of both edits") {
    assert(StringCosts.change(TestNode("fair"), TestNode("fourteen")) == 6)
  }
}
