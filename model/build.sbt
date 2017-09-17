name := "model-component"

// ScalaTest unit testing framework
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Cats for functional programming utilities
libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"

// Apache POI for accessing Microsoft Office documents
libraryDependencies ++= Seq(
  "org.apache.poi" % "poi" % "3.16",
  "org.apache.poi" % "poi-scratchpad" % "3.16"
)
