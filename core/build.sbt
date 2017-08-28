name := name.value + "-core"

maintainer := "Adrien Mannocci <adrien.mannocci@gmail.com>"

// Dependencies version
lazy val akkaVersion = "2.5.4"
lazy val playVersion = "2.6.3"
lazy val logbackVersion = "1.2.3"
lazy val logbackContribVersion = "0.3.0"
lazy val commonsLangVersion = "3.6"
lazy val metricsScalaVersion = "3.5.9"
lazy val metricsJvmVersion = "3.2.3"
lazy val guavaVersion = "23.0"

// Custom resolvers
resolvers ++= Seq(
  "Techcode" at "https://nexus.techcode.io/repository/maven-public"
)

// All libraries
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.play" %% "play-json" % playVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.commons" % "commons-lang3" % commonsLangVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "io.techcode.logback.contrib" % "logback-json-layout" % logbackContribVersion,
  "com.google.guava" % "guava" % guavaVersion,
  "nl.grons" %% "metrics-scala" % metricsScalaVersion,
  "io.dropwizard.metrics" % "metrics-jvm" % metricsJvmVersion
)

// Jmh settings
sourceDirectory in Jmh := new File((sourceDirectory in Test).value.getParentFile, "bench")
classDirectory in Jmh := (classDirectory in Test).value
dependencyClasspath in Jmh := (dependencyClasspath in Test).value

// Debian packaging
packageSummary := "Streamy Package"
packageDescription := "Transport and process your logs, events, or other data"

// Enable some plugins
enablePlugins(JavaAppPackaging, JmhPlugin)