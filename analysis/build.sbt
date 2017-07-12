name := "analysis"

version := "1.0"

scalaVersion := "2.11.11"

// ScalaTest unit testing framework
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Cats for functional programming utilities
libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"

// Apache POI for accessing Microsoft Office documents
libraryDependencies ++= Seq(
  "org.apache.poi" % "poi" % "3.16",
  "org.apache.poi" % "poi-scratchpad" % "3.16"
)

// Spark libraries
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.1" % "provided"

lazy val imllib = (project in file("imllib-spark"))
lazy val root = (project in file(".")).dependsOn(imllib)
