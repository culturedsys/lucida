package model

import java.io.InputStream

import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel

import scala.util.Try
import scala.util.matching.Regex

/**
  * An object for extracting features from Word Doc files
  */
object DocExtractor extends Extractor {

  /**
    * Sequences of dotted numbers are used as a possible sign of sub and sub-subheads.
    */
  val POSSIBLE_SUBHEAD_RE = new Regex("[0-9]+\\.[0-9]+")
  val POSSIBLE_SUBSUBHEAD_RE = new Regex("[0-9]+\\.[0-9]+\\.[0-9]+")


  /**
    * Estimate the font size of a paragraph, in points. A paragraph can use more than one font
    * size; this method attempts to find the size of the largest run in the paragraph
    */
  def estimateFontSize(para: usermodel.Paragraph): Int =
    (0 until para.numCharacterRuns()).foldLeft((0, 0)){ (acc, index) =>
      val (longest, size) = acc
      val run = para.getCharacterRun(index)
      val length = run.text().length
      if (length > longest)
        (length, run.getFontSize)
      else
        (longest, size)
    }._2 / 2 // Note the division by two, because Word stores font sizes as half points

  /**
    * Estimates whether a paragraph is in bold, simply by checking for the presence of bold.
    */
  def estimateBold(para: usermodel.Paragraph): Boolean =
    (0 until para.numCharacterRuns()).exists(para.getCharacterRun(_).isBold)

  /**
    * Estimates whether a paragraph is in italic, simply by checking for the presence of italic.
    */
  def estimateItalic(para: usermodel.Paragraph): Boolean =
    (0 until para.numCharacterRuns()).exists(para.getCharacterRun(_).isItalic)

  /**
    * Estimates the font family of this paragraph, by considering the family of the first
    * character run.
    */
  def estimateFontFamily(para: usermodel.Paragraph): String =
    para.getCharacterRun(0).getFontName

  /**
    * Estimates the alignment of a paragraph (which is directly recorded in the POI paragraph
    * object/
    */
  def estimateAlignment(para: usermodel.Paragraph): String =
    para.getJustification.toString

  /**
    * Extract a sequence of Paragraph objects from an InputStream containing a doc
    */
  def extract(stream: InputStream): Try[IndexedSeq[Paragraph]] =
    Try(new HWPFDocument(stream)).map { document =>
      val documentLength = document.getRange.numParagraphs()
      val paragraphs = (0 until documentLength).map(document.getRange.getParagraph(_))

      val fontSizes = paragraphs.map(estimateFontSize)

      val mainFontSize: Int = fontSizes.foldLeft(Map[Int, Int]().withDefaultValue(0)) {
        (m, i) => m + (i -> (m(i) + 1))
      }.toSeq.maxBy(_._2)._1

      val largestFontSizes =
        fontSizes.distinct.sorted.reverse.takeWhile(_ > mainFontSize).take(Paragraph.DISTINCT_SIZES)


      paragraphs.foldLeft((Vector[Paragraph](), 0, None: Option[usermodel.Paragraph])){
        (acc, para) =>
        val (processed, index, last) = acc
        val text = para.text()

        val words = extractWords(text)

        val position = (index / documentLength) * Paragraph.POSITION_BINS
        val possibleSubhead = POSSIBLE_SUBHEAD_RE.findFirstIn(text).isDefined
        val possibleSubSubhead = POSSIBLE_SUBSUBHEAD_RE.findFirstIn(text).isDefined
        val possibleEmail = text.contains("@")
        val possibleWeb = text.contains("http") || text.contains("www")
        val length = math.min(text.split(" ").length, Paragraph.LENGTH_MAX)
        val currentFontSize = fontSizes(index)
        val fontSize =
          if (currentFontSize < mainFontSize)
            Smaller
          else if (currentFontSize == mainFontSize)
            Common
          else
            largestFontSizes.zipWithIndex.find(_._1 == currentFontSize) match {
              case None => Larger
              case Some((_, index)) => RelativeSize(index)
            }

        val isBold = estimateBold(para)
        val isItalic = estimateItalic(para)
        val isBullet = para.isInList
        val isSameAsPrevious = last match {
          case None => true
          case Some(lastPara) =>
            isBold == estimateBold(lastPara) &&
              isItalic == estimateItalic(lastPara) &&
              fontSizes(index) == fontSizes(index - 1) &&
              estimateFontFamily(para) == estimateFontFamily(lastPara) &&
              estimateAlignment(para) == estimateAlignment(lastPara)
        }

        val features = Paragraph(
          text.split(' ').take(10).mkString,
          words,
          position,
          if (possibleSubSubhead)
            PossibleSubsubsection
          else if (possibleSubhead)
            PossibleSubsection
          else
            NumberOther,
          if (possibleEmail)
            PossibleEmail
          else if (possibleWeb)
            PossibleWeb
          else
            NetOther,
          length,
          fontSize,
          isBold,
          isItalic,
          isBullet,
          isSameAsPrevious,
          None
        )

        (processed :+ features, index + 1, Some(para))
      }._1
    }

    def extractWords(text: String): Seq[Word] = {
      // We need exactly Paragraph.WORDS words, adding the word EMPTY if necessary
      val paddedWords = (text.trim.split(" ") ++ Seq.fill(Paragraph.WORDS)("EMPTY")).take(Paragraph
        .WORDS)

      paddedWords.map(Word(_))
    }
}
