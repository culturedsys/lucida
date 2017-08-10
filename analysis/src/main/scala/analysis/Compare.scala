package analysis

import java.io.FileInputStream

import com.intel.imllib.crf.nlp.CRFModel
import model.{DocExtractor, Paragraph, Structure}

/**
  * Compare two documents and print out their structures
  */
object Compare {
  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      println("Compare: <model path> <from file path> <to file path>")
      System.exit(1)
    }

    val model = CRFModel.loadBinaryFile(args(0))
    val fromDoc = DocExtractor.extract(new FileInputStream(args(1))).get
    val toDoc = DocExtractor.extract(new FileInputStream(args(2))).get

    val (fromChanges, toChanges) = Analysis.compare(fromDoc, toDoc, model)

    output(fromChanges)
    output(toChanges)
  }

  def output(structure: Structure[(Paragraph, Change)], depth: Int = 0): Unit = {
    print(structure.label._2 match {
      case Inserted => "+"
      case Deleted => "-"
      case Changed => ">"
      case Unchanged => "#"
    })
    print(" " * (depth + 1))
    println(structure.label._1.description)
    structure.children.foreach(output(_, depth + 1))
  }
}
