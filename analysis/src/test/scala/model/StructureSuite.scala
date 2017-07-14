package model

import model.TaggedParagraph.addTag
import org.scalatest.{Args, FunSuite, Status}

/**
  * Tests for the Structure representation and extraction module.
  */
class StructureSuite extends FunSuite {

  val paragraph = new Paragraph(
    "Some text",
    Seq(),
    0,
    NumberOther,
    NetOther,
    0,
    Common,
    false,
    false,
    false,
    false)

  test("A document with no headings has a flat structure") {
    val document = Seq(
      addTag(paragraph, BodyText),
      addTag(paragraph, BodyText),
      addTag(paragraph, BodyText),
      addTag(paragraph, BodyText)
    )
    val structure = Structure.fromParagraphs(document)
    assert(structure.size === document.size)
    assert(structure.forall(_.content.description === paragraph.description))
  }

  test("A document with a title and some text has all paragraphs nested under the title") {
    val title = "A title"
    val document = Seq(
      addTag(paragraph.copy(description = title), Title),
      addTag(paragraph, BodyText),
      addTag(paragraph, BodyText),
      addTag(paragraph, BodyText)
    )

    val structure = Structure.fromParagraphs(document)

    assert(structure.size === 1)
    assert(structure.head.content.description === title)
    assert(structure.head.children.size === 3)
    assert(structure.head.children.forall(_.content.description === paragraph.description))
  }

  test("A document with subsections puts them under the sections") {
    val section = "A Section"
    val subsection = "A Subsection"
    val document = Seq(
      addTag(paragraph.copy(description = section), SectionHeader),
      addTag(paragraph, BodyText),
      addTag(paragraph.copy(description = subsection), SubsectionHeader),
      addTag(paragraph, BodyText)
    )

    val structure = Structure.fromParagraphs(document)

    assert(structure.size === 1)
    assert(structure.head.children.size === 2)
    assert(structure.head.children.head.children.isEmpty)
    assert(structure.head.children(1).content.description === subsection )
    assert(structure.head.children(1).children.size === 1)
  }

  test("A document with two headings at same level splits body text between them") {
    val section1 = "Section 1"
    val inSection1 = "In Section 1"
    val section2 = "Section 2"
    val inSection2 = "In Section 2"

    val document = Seq(
      addTag(paragraph.copy(description = section1), SectionHeader),
      addTag(paragraph.copy(description = inSection1), BodyText),
      addTag(paragraph.copy(description = section2), SectionHeader),
      addTag(paragraph.copy(description = inSection2), BodyText)
    )

    val structure = Structure.fromParagraphs(document)

    assert(structure.size === 2)
    assert(structure.head.content.description === section1)
    assert(structure.head.children.size === 1)
    assert(structure.head.children.head.content.description === inSection1)
    assert(structure(1).content.description === section2)
    assert(structure(1).children.size === 1)
    assert(structure(1).children.head.content.description === inSection2)
  }

  test("A section following a subsection moves up a level") {
    val section1 = "Section 1"
    val subsection = "Subsection"
    val section2 = "Section 2"

    val document = Seq(
      addTag(paragraph.copy(description = section1), SectionHeader),
      addTag(paragraph.copy(description = subsection), SubsectionHeader),
      addTag(paragraph.copy(description = section2), SectionHeader)
    )

    val structure = Structure.fromParagraphs(document)

    assert(structure.size === 2)
    assert(structure.head.content.description === section1)
    assert(structure.head.children.size === 1)
    assert(structure.head.children.head.content.description === subsection)
    assert(structure(1).children.isEmpty)
    assert(structure(1).content.description === section2)
  }
}
