package analysis

import java.io.{ByteArrayInputStream, FileInputStream, FileNotFoundException, IOException}
import java.nio.file.{Files, Paths}

import com.intel.imllib.crf.nlp.{CRFModel, Sequence, Token}
import model.{DocExtractor, Structure, Tag, TaggedParagraph, FeatureTemplate, Features}

/**
  * An application to predict the structure of supplied documents (and time how long it takes to
  * do so).
  */
object Predict {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Supply paths to the model and a doc file")
      System.exit(1)
    }

    val modelPath = args(0)
    val docPath = args(1)

    try {
      val model = CRFModel.loadStream(new FileInputStream(modelPath))
      val docData = Files.readAllBytes(Paths.get(docPath))

      val paras = DocExtractor.extract(new ByteArrayInputStream(docData)).get
      val seq = Sequence(FeatureTemplate.tokensAsStrings(Features.templates, paras)
                .map(Token.put).toArray)
      val start = System.currentTimeMillis()
      val Array(tags) = model.predict(Array(seq))
      val end = System.currentTimeMillis()
      val taggedParas = paras.zip(tags.sequence).map {
        case (para, token) => TaggedParagraph.addTag(para, Tag.fromString(token.label))
      }
      output(Structure.fromParagraphs(taggedParas), 0)
      println(s"Time taken in prediction: ${end - start} ms")
    } catch {
      case e: IOException =>
        println(s"Could not read data: ${e.getMessage}")
        System.exit(1)
    }

  }

  def output(structure: Seq[Structure[TaggedParagraph]], level: Int): Unit = {
    for (s <- structure) {
      println(s.content.tag.toString.padTo(10, " ").take(10)
        .mkString + ": " + ("--" * level) + s.content.description)
      output(s.children, level + 1)
    }
  }
}