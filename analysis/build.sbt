name := "analysis"

version := "1.0"

scalaVersion := "2.11.11"

lazy val imllib = (project in file("imllib-spark"))
lazy val model = (project in file("model"))
lazy val analysis = (project in file("analysis")).dependsOn(imllib).dependsOn(model)
lazy val training = (project in file("training")).dependsOn(imllib).dependsOn(model)
lazy val root = (project in file(".")).dependsOn(imllib).dependsOn(analysis)
