name := "coordinator-component"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test"

libraryDependencies += "com.sun.mail" % "javax.mail" % "1.6.0" % "test"
