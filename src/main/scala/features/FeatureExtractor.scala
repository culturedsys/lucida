package features

import java.io.InputStream

/**
  * Singleton object exposing the feature extraction method.
  */
object FeatureExtractor {
  /**
    * Extract a sequence of Paragraph objects from an InputStream containing a docx
    */
  def extract(stream: InputStream): IndexedSeq[Paragraph] = IndexedSeq.empty

}
