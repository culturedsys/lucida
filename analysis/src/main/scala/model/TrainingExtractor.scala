package model
import java.io.InputStream

import scala.io.Source
import scala.util.Try

/**
  * Extract the document features we are interested in from the SectLabel labelled training data.
  */
object TrainingExtractor extends Extractor {
  // Indexes of the fields we're interested in in the SectLabel training data
  val DESCRIPTION = 0
  val NUMBER_HINT = 2
  val NET_HINT = 3
  val LOCATION = 4
  val LENGTH = 5
  val BOLD = 7
  val ITALIC = 8
  val FONT_SIZE = 9
  val BULLET = 12
  val SAME = 13
  val PARA_NEW = 14

  val FIRST_TOKEN = 15
  val TOKEN_CASE = 12
  val TOKEN_PUNCT = 21
  val TOKEN_NUMBER = 13
  val TOKEN_LENGTH = 22

  val TAG = 103

  val RELATIVE_SIZE_RE = "xmlFontSize_largest-([0-9])".r

  override def extract(inputStream: InputStream): Try[Seq[Paragraph]] = Try {
    extract(Source.fromInputStream(inputStream).getLines().toSeq)
  }

  def extract(lines: Seq[String]): Seq[Paragraph] = {
    Util.groupedWhile(lines.map(_.split(" ")))(Equiv.fromFunction { (first, second) =>
      // Consider two lines as in the same paragraph if they have the same tag and the second
      // line is not explicitly marked as the start of a new paragraph
      first(TAG) == second(TAG) && second(PARA_NEW) != "bi_xmlPara_new"
    }).map { lines =>

      // Extract the word-level tokens from the first line (as we only need 4
      val words = extractWords(lines.head)

      // Convert the string POS-n to the number n
      val location = lines.head(LOCATION).substring(4).toInt

      // As we mostly care about things like 1.1 at the beginning of a heading, use the number
      // hint value of the first line in the paragraph
      val numberHint = lines.head(NUMBER_HINT) match {
        case "posSubsec" => PossibleSubsection
        case "posSubsubsec" => PossibleSubsubsection
        case _ => NumberOther
      }

      // Net lines are typically single-line paragraphs, so again use the first line
      val netHint = lines.head(NET_HINT) match {
        case "possibleEmail" => PossibleEmail
        case "possibleWeb" => PossibleWeb
        case _ => NetOther
      }

      // Convert the string nWords to the number n, add up for all the lines, and cap at the
      // maximum length.
      val length = math.min(lines.map(_(LENGTH).substring(0, 1).toInt).sum, Paragraph.LENGTH_MAX)

      // Take the largest font size in the paragraph as the fontSize of the whole paragraph
      val fontSize = lines.map(_(FONT_SIZE) match {
        case RELATIVE_SIZE_RE(size) => RelativeSize(size.toInt)
        case "xmlFontSize_larger" => Larger
        case "xmlFontSize_smaller" => Smaller
        case _ => Common
      }).max

      // Treat a paragraph containing any bold, italic, or bulletted lines as bold, italic, or
      // bulletted, respectively.
      val isBold = lines.exists(_(BOLD) == "xmlBold_yes")
      val isItalic = lines.exists(_(ITALIC) == "xmlItalic_yes")
      val isBullet = lines.exists(_(BULLET) == "xmlBullet_yes")

      // The same/different formatting quality is paragraph-level, so we can just use the value
      // for the first line.
      val isSameAsPrevious = lines.head(SAME) == "bi_xmlSFBIA_continue"

      Paragraph(
        lines.head(DESCRIPTION),
        words,
        location,
        numberHint,
        netHint,
        length,
        fontSize,
        isBold,
        isItalic,
        isBullet,
        isSameAsPrevious,
        Tag.fromString.lift(lines.head(TAG))
      )
    }
  }

  def extractWords(attributes: Array[String]): Seq[Word] = {
    (0 until Paragraph.WORDS).map { index =>
      val base = FIRST_TOKEN + TOKEN_LENGTH * index
      val word = attributes(base).replaceFirst("^TOKEN-", "")
      val lowerCase = word.toLowerCase
      val unpunctuated = lowerCase.filter(_.isLetterOrDigit)
      val wordCase = attributes(base + TOKEN_CASE) match {
        case "InitCap" => InitialCaps
        case "singleCap" => InitialCaps
        case "AllCap" => AllCaps
        case "MixedCap" => MixedCaps
        case _ => OtherCaps
      }
      val digits = attributes(base + TOKEN_NUMBER) match {
        case "1dig" => JustDigits(1)
        case "2dig" => JustDigits(2)
        case "3dig" => JustDigits(3)
        case "4+dig" => JustDigits(4)
        case "hasDig" => HasDigits
        case "nonNum" => NoDigits
        case _ => OtherDigits
      }

      Word(word, lowerCase, unpunctuated, wordCase, digits)
    }
  }
}
