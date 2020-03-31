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

  val AkkaHttpVersion = "10.1.11"
  val AkkaKafkaVersion = "2.0.2"
  val AkkaStreamKafkaVersion = "2.0.2"
  val AkkaVersion = "2.5.29"
  val ElasticClientVersion = "7.3.0"
  val GuavaVersion = "28.2-jre"
  val LogbackClassicVersion = "1.2.3"
  val MetricJvmVersion = "4.0.5"
  val MockitoCoreVersion = "2.23.4"
  val PureConfig = "0.12.3"
  val ScalaTest = "3.0.8"
  val TestContainersScalaVersion = "0.36.1"
  val TestContainersVersion = "1.13.0"

  object Compile {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion // Apache 2 License
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion // Apache 2 License
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion // Apache 2 License
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion // Apache 2 License
    val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % AkkaStreamKafkaVersion // Apache 2 License
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion // Apache 2 License
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion // Apache 2 License
    val googleGuava = "com.google.guava" % "guava" % GuavaVersion // Apache 2 License
    val logbackClassic = "ch.qos.logback" % "logback-classic" % LogbackClassicVersion // EPL/LGPL License
    val metricsJvm = "io.dropwizard.metrics" % "metrics-jvm" % MetricJvmVersion // Apache 2 License
    val mockitoCore = "org.mockito" % "mockito-core" % MockitoCoreVersion
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfig // Mozilla Public License 2.0
    val scalaTest = "org.scalatest" %% "scalatest" % ScalaTest
  }

  object Test {
    val akkaStreamKafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit" % AkkaKafkaVersion % "test"
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test" // Apache 2 License
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test" // Apache 2 License
    val elasticClient = "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % ElasticClientVersion % "test"
    val mockitoCore = "org.mockito" % "mockito-core" % MockitoCoreVersion % "test"
    val scalaTest = "org.scalatest" %% "scalatest" % ScalaTest % "test"
    val testContainers = "com.dimafeng" %% "testcontainers-scala-scalatest" % TestContainersScalaVersion % "test"
    val testContainersElastic = "com.dimafeng" %% "testcontainers-scala-elasticsearch" % TestContainersScalaVersion % "test"
    val testContainersKafka = "org.testcontainers" % "kafka" % TestContainersVersion % "test"
  }

  import Compile._
  import Test._

  val core = libraryDependencies ++= Seq(
    akkaActor, akkaStream, akkaSlf4j, logbackClassic, googleGuava, pureConfig,
    Test.akkaTestkit, Test.akkaStreamTestkit, Test.mockitoCore, Test.scalaTest
  )

  val elasticsearch = libraryDependencies ++= Seq(akkaHttp, elasticClient, testContainers, testContainersElastic)

  val kafka = libraryDependencies ++= Seq(akkaStreamKafka, testContainers, testContainersKafka, akkaStreamKafkaTestkit)

  val metric = libraryDependencies ++= Seq(metricsJvm)

  val tcp = libraryDependencies ++= Seq(testContainers)

  val testkit = libraryDependencies ++= Seq(
    Compile.mockitoCore, Compile.scalaTest,
    Compile.akkaTestkit, Compile.akkaStreamTestkit
  )

}
