name := """Coordinator"""
//organization := "lucida"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "lucida.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "lucida.binders._"
