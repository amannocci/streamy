/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

  val AkkaVersion = "2.5.29"
  val AkkaKafkaVersion = "2.0.2"
  val TestContainersVersion = "1.13.0"
  val TestContainersScalaVersion = "0.36.1"

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" // Apache 2 License
    val akkaStream = "com.typesafe.akka" %% "akka-stream" // Apache 2 License
    val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" // Apache 2 License
    val akkaHttp = "com.typesafe.akka" %% "akka-http" // Apache 2 License
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" // Apache 2 License
    val logbackClassic = "ch.qos.logback" % "logback-classic" // EPL/LGPL License
    val googleGuava = "com.google.guava" % "guava" // Apache 2 License
    val pureConfig = "com.github.pureconfig" %% "pureconfig" // Mozilla Public License 2.0
    val scalaReflect = Def.setting {
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    }
    val metricsJvm = "io.dropwizard.metrics" % "metrics-jvm" // Apache 2 License
  }

  object Test {
    val testContainers = "com.dimafeng" %% "testcontainers-scala-scalatest"
    val testContainersElastic = "com.dimafeng" %% "testcontainers-scala-elasticsearch"
    val testContainersKafka = "org.testcontainers" % "kafka"
    val elastic = "org.elasticsearch.client" % "elasticsearch-rest-high-level-client"
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" // Apache 2 License
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
    val akkaStreamkafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit"
    val scalaTest = "org.scalatest" %% "scalatest"
    val mockitoCore = "org.mockito" % "mockito-core"
  }

  import Compile._, Test._

  val akka = libraryDependencies ++= Seq(akkaActor, akkaStream, akkaSlf4j).map(_ % AkkaVersion)
  val akkaStreamKafka = libraryDependencies ++= Seq(Compile.akkaStreamKafka).map(_ % "2.0.2")
  val akkaHttp = libraryDependencies ++= Seq(Compile.akkaHttp).map(_ % "10.1.11")
  val logback = libraryDependencies ++= Seq(logbackClassic % "1.2.3")
  val guava = libraryDependencies ++= Seq(googleGuava % "28.2-jre")
  val config = libraryDependencies ++= Seq(pureConfig % "0.12.3")
  val scala = libraryDependencies ++= Seq(scalaReflect.value)
  val metric = libraryDependencies ++= Seq(metricsJvm % "4.0.5")

  private val akkaTesting = Seq(akkaTestkit, akkaStreamTestkit).map(_ % AkkaVersion)
  val akkaTest = libraryDependencies ++= akkaTesting.map(_ % "test")
  val akkaTestLib = libraryDependencies ++= akkaTesting

  val testKit = libraryDependencies ++= Seq(scalaTest % "3.0.8", mockitoCore % "2.23.4")

  val elasticTest = libraryDependencies ++= Seq(
    elastic % "7.3.0",
    testContainers % TestContainersScalaVersion,
    testContainersElastic % TestContainersScalaVersion
  ).map(_ % "test")

  val kafkaTest = libraryDependencies ++= Seq(
    akkaStreamTestkit % AkkaVersion,
    akkaStreamkafkaTestkit % AkkaKafkaVersion,
    testContainersKafka % TestContainersVersion
  ).map(_ % "test")

  val tcpTest = libraryDependencies ++= Seq(
    testContainers % TestContainersScalaVersion
  ).map(_ % "test")

}
