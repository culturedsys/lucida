name := "lucida"

version := "1.0.0"

scalaVersion := "2.11.11"

lazy val commonSettings = Seq(
  version := "1.0.1",
  scalaVersion := "2.11.11"
)

lazy val imllib = (project in file("imllib-spark")).settings(commonSettings)
lazy val model = (project in file("model")).settings(commonSettings)
lazy val training = (project in file("training")).dependsOn(imllib)
                                                    .dependsOn(model)
                                                    .settings(commonSettings)
lazy val protocol = (project in file("protocol")).enablePlugins(PlayScala)
                                                    .settings(commonSettings)
lazy val analysis = (project in file("analysis")).dependsOn(imllib)
                                                    .dependsOn(model)
                                                    .dependsOn(protocol)
                                                    .settings(commonSettings)
lazy val interface = (project in file("interface")).settings(commonSettings)
lazy val coordinator = (project in file("coordinator")).dependsOn(protocol)
                                                          .aggregate(interface)
                                                          .enablePlugins(PlayScala)
                                                          .settings(commonSettings)
