name := "training-component"

version := "1.0.0"

scalaVersion := "2.11.11"

// ScalaTest unit testing framework
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Spark libraries
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.1" % "provided"
