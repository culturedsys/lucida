name := "features"

version := "1.0"

scalaVersion := "2.11.11"

// Apache POI for accessing Microsoft Office documents
libraryDependencies ++= Seq(
  "org.apache.poi" % "poi" % "3.16",
  "org.apache.poi" % "poi-ooxml" % "3.16",
  "org.apache.poi" % "poi-ooxml-schemas" % "3.16"
)
    