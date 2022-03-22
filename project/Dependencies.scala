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

  val AkkaHttpVersion = "10.2.7"
  val AkkaStreamKafkaVersion = "2.1.1"
  val AkkaVersion = "2.6.17"
  val Elastic4sVersion = "7.16.0"
  val GuavaVersion = "31.0.1-jre"
  val LogbackClassicVersion = "1.2.9"
  val MockitoCoreVersion = "3.6.28"
  val PureConfigVersion = "0.17.1"
  val BorerVersion = "1.7.2"
  val CirceVersion = "0.14.1"
  val JacksonVersion = "2.13.0"
  val ScalaTest = "3.2.10"
  val TestContainersScalaVersion = "0.39.11"
  val TestContainersVersion = "1.16.2"

  object Compile {
    val akkaActor = ("com.typesafe.akka" %% "akka-actor" % AkkaVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val akkaHttp = ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val akkaSlf4j = ("com.typesafe.akka" %% "akka-slf4j" % AkkaVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val akkaStream = ("com.typesafe.akka" %% "akka-stream" % AkkaVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val akkaStreamKafka = ("com.typesafe.akka" %% "akka-stream-kafka" % AkkaStreamKafkaVersion).excludeAll(
      ExclusionRule("com.typesafe.akka", "akka-stream_2.13"),
      ExclusionRule("org.slf4j", "slf4j-api")
    ) // Apache 2 License
    val akkaStreamTestkit = ("com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val akkaTestkit = ("com.typesafe.akka" %% "akka-testkit" % AkkaVersion).excludeAll(
      ExclusionRule("com.typesafe", "config")
    ) // Apache 2 License
    val googleGuava = "com.google.guava" % "guava" % GuavaVersion // Apache 2 License
    val logbackClassic = "ch.qos.logback" % "logback-classic" % LogbackClassicVersion // EPL/LGPL License
    val mockitoCore = "org.mockito" % "mockito-core" % MockitoCoreVersion
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfigVersion // Mozilla Public License 2.0
    val scalaTest = "org.scalatest" %% "scalatest" % ScalaTest
    val borer = "io.bullet" %% "borer-core" % BorerVersion
    val borerDerivation = "io.bullet" %% "borer-derivation" % BorerVersion
    val borerCompatAkka = "io.bullet" %% "borer-compat-akka" % BorerVersion
    val circeCore = "io.circe" %% "circe-core" % CirceVersion
    val circeGeneric = "io.circe" %% "circe-generic" % CirceVersion
    val circeParser = "io.circe" %% "circe-parser" % CirceVersion
    val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion
    val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion
  }

  object Test {
    val akkaStreamKafkaTestkit = "com.typesafe.akka" %% "akka-stream-kafka-testkit" % AkkaStreamKafkaVersion % "test"
    val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test" // Apache 2 License
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test" // Apache 2 License

    val elasticClient = "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Elastic4sVersion % "test"
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

  val bench = libraryDependencies ++= Seq(
    borer, borerDerivation, borerCompatAkka, circeCore, circeGeneric, circeParser, jacksonCore, jacksonDatabind
  )

  val elasticsearch = libraryDependencies ++= Seq(akkaHttp, elasticClient, testContainers, testContainersElastic)

  val kafka = libraryDependencies ++= Seq(akkaStreamKafka, testContainers, testContainersKafka, akkaStreamKafkaTestkit)

  val tcp = libraryDependencies ++= Seq(testContainers)

  val testkit = libraryDependencies ++= Seq(
    Compile.mockitoCore, Compile.scalaTest,
    Compile.akkaTestkit, Compile.akkaStreamTestkit
  )

}
