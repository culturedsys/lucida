package features

import java.io.InputStream

import scala.util.Try

/**
  * A trait for the shared features of extractors for different formats.
  */
trait FeatureExtractor {
  /**
    * Produce a sequence of Paragraph representations, on the basis of information read from an
    * InputStream.
    */
  def extract(inputStream: InputStream): Try[IndexedSeq[Paragraph]]
}
