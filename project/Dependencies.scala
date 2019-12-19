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

object Dependencies {

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" // Apache 2 License
    val akkaStream = "com.typesafe.akka" %% "akka-stream" // Apache 2 License
    val akkaHttp = "com.typesafe.akka" %% "akka-http" // Apache 2 License
    val akkaSlf4j =  "com.typesafe.akka" %% "akka-slf4j" // Apache 2 License
    val logbackClassic =  "ch.qos.logback" % "logback-classic" // EPL/LGPL License
    val googleGuava = "com.google.guava" % "guava" // Apache 2 License
    val pureConfig = "com.github.pureconfig" %% "pureconfig" // Mozilla Public License 2.0
    val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect"  % scalaVersion.value }
    val metricsJvm = "io.dropwizard.metrics" % "metrics-jvm" // Apache 2 License
  }

  object Test {
    val elastic = "org.elasticsearch.client" % "elasticsearch-rest-high-level-client"
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" // Apache 2 License
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
    val scalaTest = "org.scalatest" %% "scalatest"
    val mockitoCore = "org.mockito" % "mockito-core"
  }

  import Compile._, Test._

  val akka = libraryDependencies ++= Seq(akkaActor,akkaStream, akkaSlf4j).map(_ % "2.5.27")
  val akkaHttp = libraryDependencies ++= Seq(Compile.akkaHttp).map(_ % "10.1.11")
  val logback = libraryDependencies ++= Seq(logbackClassic % "1.2.3")
  val guava = libraryDependencies ++= Seq(googleGuava % "28.1-jre")
  val config = libraryDependencies ++= Seq(pureConfig % "0.12.1")
  val scala = libraryDependencies ++= Seq(scalaReflect.value)
  val metric = libraryDependencies ++= Seq(metricsJvm % "4.0.5")

  private val akkaTesting = Seq(akkaTestkit, akkaStreamTestkit).map(_ % "2.5.27")
  val akkaTest = libraryDependencies ++= akkaTesting.map(_ % "test")
  val akkaTestLib = libraryDependencies ++= akkaTesting
  val elasticTest = libraryDependencies ++= Seq(elastic).map(_ % "7.3.0" % "test")
  val testKit = libraryDependencies ++= Seq(scalaTest % "3.0.8", mockitoCore % "2.23.4")

}
