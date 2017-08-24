package analysis

import java.io.{FileInputStream, FileOutputStream}

import com.intel.imllib.crf.nlp.CRFModel

/**
  * Created by tim on 21/08/17.
  */
object Rewrite {
  def main(args: Array[String]): Unit = {
    val model = CRFModel.loadBinaryFile("/home/tim/src/msc/lucida/target/model")
    val out = new FileOutputStream("/tmp/model")
    CRFModel.saveStream(model, out)
    out.close()

    val in = new FileInputStream("/tmp/model")
    val loaded = CRFModel.loadStream(in)
    in.close()

    assert(model.alpha sameElements loaded.alpha)
  }
}
