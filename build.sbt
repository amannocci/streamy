/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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

import sbt.Keys._
import sbt._


// Common settings
ThisBuild / version := "0.13.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "io.techcode.streamy"

// Disable parallel execution
concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)

lazy val commonSettings = Seq(
  // Disable test in assembly
  test in assembly := {},

  // Scala compiler options
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  ),

  scriptClasspath := Seq("*", "../plugin/*")
)

lazy val bench = project
  .in(file("bench"))
  .settings(
    publish := {},
    publishLocal := {}
  )
  .dependsOn(
    core % "test->test",
    `plugin-date` % "test->test",
    `plugin-elasticsearch` % "test->test",
    `plugin-fingerprint` % "test->test",
    `plugin-gelf` % "test->test",
    `plugin-graphite` % "test->test",
    `plugin-json` % "test->test",
    `plugin-protobuf` % "test->test",
    `plugin-syslog` % "test->test",
    `plugin-tcp` % "test->test",
    `plugin-xymon` % "test->test"
  )
  .settings(Dependencies.bench)
  .settings(Benchs.settings)
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(JmhPlugin)

lazy val core = project
  .in(file("core"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,
    bashScriptExtraDefines +=
      """
        |source ./bin/make-default.sh
        |source ./bin/setup-container.sh
        |source ./bin/make-template.sh
        |
        |make_default
        |setup_container
        |make_template
      """.stripMargin,
    fork := true,
    javaOptions in Test += s"-Duser.dir=${baseDirectory.in(ThisBuild).value}/core/runtime",

    maintainer := "Adrien Mannocci <adrien.mannocci@gmail.com>",
    packageSummary := "High Performance events processing",
    packageDescription := "Transport and process your logs, events, or other data"
  )
  .settings(Dependencies.core)
  .settings(Publish.settings)
  .enablePlugins(JavaServerAppPackaging, SystemdPlugin, SystemloaderPlugin)
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-date` = project
  .in(file("plugin-date"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-elasticsearch` = project
  .in(file("plugin-elasticsearch"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,

    // Don't include scala in assembly
    assemblyOption in assembly ~= {
      _.copy(includeScala = false)
    },

    assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp.filter { x =>
        x.data.getName.startsWith("scala-library")
      }
    },

    // Publish fat jars
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in(Compile, assembly), assembly)
  )
  .settings(Dependencies.elasticsearch)
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")

lazy val `plugin-fingerprint` = project
  .in(file("plugin-fingerprint"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-gelf` = project
  .in(file("plugin-gelf"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile", `plugin-tcp` % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-graphite` = project
  .in(file("plugin-graphite"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile", `plugin-tcp` % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-json` = project
  .in(file("plugin-json"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-kafka` = project
  .in(file("plugin-kafka"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,

    // Don't include scala in assembly
    assemblyOption in assembly ~= {
      _.copy(includeScala = false)
    },

    assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp.filterNot { x =>
        x.data.getName.startsWith("classes") ||
          x.data.getName.startsWith("akka-stream-kafka") ||
          x.data.getName.startsWith("kafka-clients") ||
          x.data.getName.startsWith("zstd-jni") ||
          x.data.getName.startsWith("lz4-java") ||
          x.data.getName.startsWith("slf4j-api") ||
          x.data.getName.startsWith("snappy-java")
      }
    },

    // Publish fat jars
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in(Compile, assembly), assembly)
  )
  .settings(Dependencies.kafka)
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")

lazy val `plugin-protobuf` = project
  .in(file("plugin-protobuf"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(ProtobufPlugin)

lazy val `plugin-riemann` = project
  .in(file("plugin-riemann"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(`plugin-protobuf` % "provided->compile", `plugin-tcp` % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)
  .enablePlugins(ProtobufPlugin)

lazy val `plugin-syslog` = project
  .in(file("plugin-syslog"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile", `plugin-tcp` % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-tcp` = project
  .in(file("plugin-tcp"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Dependencies.tcp)
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-xymon` = project
  .in(file("plugin-xymon"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val testkit = project
  .in(file("testkit"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,
    coverageEnabled := false
  )
  .settings(Dependencies.testkit)
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
  .disablePlugins(AssemblyPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    publish := {},
    publishLocal := {}
  )
  .aggregate(
    bench,
    core,
    `plugin-date`,
    `plugin-elasticsearch`,
    `plugin-fingerprint`,
    `plugin-gelf`,
    `plugin-graphite`,
    `plugin-json`,
    `plugin-kafka`,
    `plugin-protobuf`,
    `plugin-riemann`,
    `plugin-syslog`,
    `plugin-tcp`,
    `plugin-xymon`,
    testkit
  )
  .disablePlugins(AssemblyPlugin)