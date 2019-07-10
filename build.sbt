/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
ThisBuild / version := "0.7.1"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / organization := "io.techcode.streamy"
ThisBuild / name := "streamy"

// Disable parallel execution
parallelExecution in ThisBuild := false

lazy val commonSettings = Seq(
  // Disable test in assembly
  test in assembly := {},

  // Scala compiler options
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  )
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
    `plugin-graphite` % "test->test",
    `plugin-json` % "test->test",
    `plugin-metric` % "test->test",
    `plugin-protobuf` % "test->test",
    `plugin-syslog` % "test->test",
    `plugin-tcp` % "test->test",
    `plugin-xymon` % "test->test"
  )
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
      """.stripMargin
  )
  .settings(
    Dependencies.akka,
    Dependencies.logback,
    Dependencies.guava,
    Dependencies.config,
    Dependencies.scala,
    Dependencies.akkaTest,
    Dependencies.testKit
  )
  .settings(Packages.settings)
  .settings(Publish.settings)
  .enablePlugins(JavaServerAppPackaging, SystemVPlugin)
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

    // Publish fat jars
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in(Compile, assembly), assembly)
  )
  .settings(Dependencies.sttp, Dependencies.elasticTest)
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

lazy val `plugin-graphite` = project
  .in(file("plugin-graphite"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
  .settings(Publish.settings)
  .dependsOn(core % "provided->compile")
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

lazy val `plugin-metric` = project
  .in(file("plugin-metric"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,

    // Don't include scala in assembly
    assemblyOption in assembly ~= {
      _.copy(includeScala = false)
    },

    // Publish fat jars
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(artifact in(Compile, assembly), assembly)
  )
  .settings(Dependencies.metric)
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
  .dependsOn(`plugin-protobuf` % "provided->compile")
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
  .dependsOn(core % "provided->compile")
  .dependsOn(testkit % "test->test")
  .disablePlugins(AssemblyPlugin)

lazy val `plugin-tcp` = project
  .in(file("plugin-tcp"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value
  )
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
  .in(file("test"))
  .settings(
    commonSettings,
    name := "streamy-" + name.value,
    coverageEnabled := false
  )
  .settings(Dependencies.akkaTestLib, Dependencies.testKit)
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
    `plugin-graphite`,
    `plugin-json`,
    `plugin-metric`,
    `plugin-protobuf`,
    `plugin-riemann`,
    `plugin-syslog`,
    `plugin-tcp`,
    `plugin-xymon`,
    testkit
  )
  .disablePlugins(AssemblyPlugin)