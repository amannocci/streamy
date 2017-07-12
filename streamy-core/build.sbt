name := name.value + "-core"

maintainer := "Adrien Mannocci <adrien.mannocci@gmail.com>"

packageSummary := "Streamy"

packageDescription := "Streamy"

lazy val akkaVersion = "2.5.3"
lazy val playVersion = "2.6.0"
lazy val logbackVersion = "1.2.3"
lazy val logbackContribVersion = "0.2.1"
lazy val riemannVersion = "0.4.5"
lazy val commonsLangVersion = "3.5"
lazy val metricsScalaVersion = "3.5.8_a2.4"
lazy val metricsJvmVersion = "3.2.2"

resolvers ++= Seq(
  "Techcode" at "https://nexus.techcode.io/repository/maven-public",
  "Clojars" at "http://clojars.org/repo"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.play" %% "play-json" % playVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.commons" % "commons-lang3" % commonsLangVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "io.riemann" % "riemann-java-client" % riemannVersion,
  "io.techcode.logback.contrib" % "logback-json-layout" % logbackContribVersion,
  "nl.grons" %% "metrics-scala" % metricsScalaVersion,
  "io.dropwizard.metrics" % "metrics-jvm" % metricsJvmVersion
)

enablePlugins(JavaAppPackaging)