package features

import java.io.InputStream

import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.usermodel

import scala.util.Try
import scala.util.matching.Regex

/**
  * An object for extracting features from Word Doc files
  */
object DocExtractor extends FeatureExtractor {

  /**
    * Paragraph position in document is scaled so that it is in a number of "bins" (rather than
    * using absolute position, which would obviously not compare well between different length
    * documents). Luoung et al. use 8 bins, which they report gave good results in experiments.
    */
  val POSITION_BINS = 8

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
      val largestFontSize = fontSizes.max
      val mainFontSize: Int = fontSizes.foldLeft(Map[Int, Int]().withDefaultValue(0)) {
        (m, i) => m + (i -> (m(i) + 1))
      }.toSeq.maxBy(_._2)._1

      paragraphs.foldLeft((Vector[Paragraph](), 0, None: Option[usermodel.Paragraph])){
        (acc, para) =>
        val (processed, index, last) = acc
        val text = para.text()

        val position = (index / documentLength) * POSITION_BINS
        val possibleSubhead = POSSIBLE_SUBHEAD_RE.findFirstIn(text).isDefined
        val possibleSubSubhead = POSSIBLE_SUBSUBHEAD_RE.findFirstIn(text).isDefined
        val possibleEmail = text.contains("@")
        val possibleWeb = text.contains("http") || text.contains("www")
        val length = text.split(" ").length
        val fontSize = if (fontSizes(index) < mainFontSize)
                         Small
                       else
                         RelativeSize(fontSizes(index) - largestFontSize)
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

        val features = Paragraph(position,
          possibleSubhead,
          possibleSubSubhead,
          possibleEmail,
          possibleWeb,
          length,
          fontSize,
          isBold,
          isItalic,
          isBullet,
          isSameAsPrevious
        )

        (processed :+ features, index + 1, Some(para))
      }._1
    }


}
