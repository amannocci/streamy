name := name.value + "-core"

maintainer := "Adrien Mannocci <adrien.mannocci@gmail.com>"

// Dependencies version
lazy val akkaVersion = "2.5.7"
lazy val logbackVersion = "1.2.3"
lazy val logbackContribVersion = "0.3.0"
lazy val commonsLangVersion = "3.6"
lazy val jacksonVersion = "2.9.2"
lazy val metricsScalaVersion = "3.5.9"
lazy val metricsJvmVersion = "3.2.3"
lazy val guavaVersion = "23.3-jre"

// Custom resolvers
resolvers ++= Seq(
  "Techcode" at "https://nexus.techcode.io/repository/maven-public"
)

// All akka libraries
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor", // Apache 2 License
  "com.typesafe.akka" %% "akka-stream", // Apache 2 License
  "com.typesafe.akka" %% "akka-slf4j" // Apache 2 License
).map(_ % akkaVersion % Compile)

// All jackson libraries
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core", // Apache 2 License
  "com.fasterxml.jackson.core" % "jackson-databind" // Apache 2 License
).map(_ % jacksonVersion)

// All other libraries
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % commonsLangVersion, // Apache 2 License
  "ch.qos.logback" % "logback-classic" % logbackVersion, // EPL/LGPL License
  "io.techcode.logback.contrib" % "logback-json-layout" % logbackContribVersion, // MIT License
  "com.google.guava" % "guava" % guavaVersion, // Apache 2 License
  "nl.grons" %% "metrics-scala" % metricsScalaVersion, // Apache 2 License
  "io.dropwizard.metrics" % "metrics-jvm" % metricsJvmVersion, // Apache 2 License
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
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