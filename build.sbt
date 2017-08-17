name := "lucida"

version := "0.1"

scalaVersion := "2.11.11"

lazy val imllib = (project in file("imllib-spark"))
lazy val model = (project in file("model"))
lazy val training = (project in file("training")).dependsOn(imllib).dependsOn(model)
lazy val protocol = (project in file("protocol")).enablePlugins(PlayScala)
lazy val analysis = (project in file("analysis")).dependsOn(imllib).dependsOn(model)
                                                  .dependsOn(protocol)
lazy val coordinator = (project in file("coordinator")).dependsOn(protocol).enablePlugins(PlayScala)
