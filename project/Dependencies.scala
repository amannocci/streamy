/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import sbt._
import sbt.Keys._

object Dependencies {

  // Dependencies version
  lazy val akkaVersion = "2.5.8"
  lazy val logbackVersion = "1.2.3"
  lazy val logbackContribVersion = "0.3.0"
  lazy val commonsLangVersion = "3.6"
  lazy val jacksonVersion = "2.9.2"
  lazy val metricsScalaVersion = "3.5.9"
  lazy val metricsJvmVersion = "3.2.3"
  lazy val guavaVersion = "23.5-jre"
  lazy val scalaTestVersion = "3.0.4"
  lazy val mockitoVersion = "2.12.0"

  val testSettings = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion,
      "org.mockito" % "mockito-core" % mockitoVersion
    ).map(_ % Test),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit", // Apache 2 License
      "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
    ).map(_ % akkaVersion % Test)
  )

}