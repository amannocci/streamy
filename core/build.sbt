/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import Dependencies._

name := name.value + "-core"

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
).map(_ % jacksonVersion % Compile)

// All other libraries
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % commonsLangVersion, // Apache 2 License
  "ch.qos.logback" % "logback-classic" % logbackVersion, // EPL/LGPL License
  "io.techcode.logback.contrib" % "logback-json-layout" % logbackContribVersion, // MIT License
  "com.google.guava" % "guava" % guavaVersion, // Apache 2 License
  "io.dropwizard.metrics" % "metrics-jvm" % metricsJvmVersion, // Apache 2 License
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion, // Mozilla Public License 2.0
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
).map(_ % Compile)

// Add container contraints detection
bashScriptExtraDefines +=
  """
    |source ./bin/setup-container.sh
    |source ./bin/make-template.sh
    |
    |setup_container
  """.stripMargin

// Enable some plugins
enablePlugins(JavaServerAppPackaging, SystemVPlugin)

// Disable some plugins
disablePlugins(AssemblyPlugin)