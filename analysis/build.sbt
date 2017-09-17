name := "analysis-component"

// ScalaTest unit testing framework
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies +=   "org.scalanlp" %% "breeze" % "0.12"

// Web service client
libraryDependencies += ws

libraryDependencies += "com.typesafe.play" %% "play-server" % "2.6.3" % "test"
libraryDependencies += "com.typesafe.play" %% "play-akka-http-server" % "2.6.3" % "test"

libraryDependencies += "commons-io" % "commons-io" % "2.5" % "test"

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("analysis.Service")