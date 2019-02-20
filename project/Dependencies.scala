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

import sbt.Keys._
import sbt._

object Dependencies {

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" // Apache 2 License
    val akkaStream = "com.typesafe.akka" %% "akka-stream" // Apache 2 License
    val akkaSlf4j =  "com.typesafe.akka" %% "akka-slf4j" // Apache 2 License
    val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" // Apache 2 License
    val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" // Apache 2 License
    val logbackClassic =  "ch.qos.logback" % "logback-classic" // EPL/LGPL License
    val googleGuava = "com.google.guava" % "guava" // Apache 2 License
    val pureConfig = "com.github.pureconfig" %% "pureconfig" // Mozilla Public License 2.0
    val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect"  % scalaVersion.value }
    val metricsJvm = "io.dropwizard.metrics" % "metrics-jvm" // Apache 2 License
    val sttpCore = "com.softwaremill.sttp" %% "core" // Apache 2 License
    val sttpBackend = "com.softwaremill.sttp" %% "akka-http-backend" // Apache 2 License
  }

  object Test {
    val elastic = "org.elasticsearch.client" % "elasticsearch-rest-high-level-client"
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" // Apache 2 License
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
    val scalaTest = "org.scalatest" %% "scalatest"
    val mockitoCore = "org.mockito" % "mockito-core"
  }

  import Compile._, Test._

  val akka = libraryDependencies ++= Seq(akkaActor, akkaStream, akkaSlf4j).map(_ % "2.5.21")
  val jackson = libraryDependencies ++= Seq(jacksonCore, jacksonDatabind).map(_ % "2.9.8")
  val logback = libraryDependencies ++= Seq(logbackClassic % "1.2.3")
  val guava = libraryDependencies ++= Seq(googleGuava % "27.0.1-jre")
  val config = libraryDependencies ++= Seq(pureConfig % "0.10.2")
  val scala = libraryDependencies ++= Seq(scalaReflect.value)
  val metric = libraryDependencies ++= Seq(metricsJvm % "4.0.2")
  val sttp = libraryDependencies ++= Seq(sttpCore, sttpBackend).map(_ % "1.5.8")

  private val akkaTesting = Seq(akkaTestkit, akkaStreamTestkit).map(_ % "2.5.21")
  val akkaTest = libraryDependencies ++= akkaTesting.map(_ % "test")
  val akkaTestLib = libraryDependencies ++= akkaTesting
  val elasticTest = libraryDependencies ++= Seq(elastic).map(_ % "6.5.4" % "test")
  val testKit = libraryDependencies ++= Seq(scalaTest % "3.0.5", mockitoCore % "2.23.4")

}
