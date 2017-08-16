name := "lucida"

version := "0.1"

scalaVersion := "2.11.11"

lazy val imllib = (project in file("imllib-spark"))
lazy val model = (project in file("model"))
lazy val analysis = (project in file("analysis")).dependsOn(imllib).dependsOn(model)
lazy val training = (project in file("training")).dependsOn(imllib).dependsOn(model)
lazy val coordinator = (project in file("coordinator"))
//lazy val root = (project in file(".")).dependsOn(analysis).dependsOn(training).dependsOn(coordinator)
