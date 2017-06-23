import org.apache.poi.xwpf.usermodel.XWPFRun

case class Test(something: Int)

val v: (Test => Int) = _.something

classOf[Test].getResource("features.docx")

