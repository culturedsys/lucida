package model

import java.io.InputStream

import scala.util.Try

/**
  * A trait for the shared functions of extractors for different formats.
  */
trait Extractor {
  /**
    * Produce a sequence of Paragraph representations, on the basis of information read from an
    * InputStream.
    */
  def extract(inputStream: InputStream): Try[Seq[Paragraph]]
}
