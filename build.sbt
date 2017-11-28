import Dependencies._
import sbt.Keys.{libraryDependencies, publishTo, scalacOptions}

lazy val commonSettings = Seq(
  name := "streamy",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4",

  // Scala compiler options
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  )
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion,
    "org.mockito" % "mockito-core" % mockitoVersion
  ).map(_ % Test),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-testkit", // Apache 2 License
    "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
  ).map(_ % akkaVersion % Test)
)

lazy val publishSettings = Seq(
  organization := "io.techcode.streamy",
  publishTo := {
    val nexus = "https://nexus.techcode.io/"
    if (isSnapshot.value) {
      Some("snapshots" at nexus + "repository/maven-snapshots")
    } else {
      Some("releases" at nexus + "repository/maven-releases")
    }
  },
  isSnapshot := true,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
)

lazy val core = (project in file("core"))
  .settings(commonSettings, dependencySettings, publishSettings)

lazy val `plugin-fingerprint` = (project in file("plugin-fingerprint"))
  .settings(commonSettings, dependencySettings, publishSettings)
  .dependsOn(core)

lazy val `plugin-syslog` = (project in file("plugin-syslog"))
  .settings(commonSettings, dependencySettings, publishSettings)
  .dependsOn(core)

lazy val `plugin-json` = (project in file("plugin-json"))
  .settings(commonSettings, dependencySettings, publishSettings)
  .dependsOn(core)

lazy val test = (project in file("test"))
  .settings(commonSettings, publishSettings)

lazy val root = (project in file("."))
  .settings(Seq(publish := {}))
  .aggregate(core, `plugin-fingerprint`, `plugin-syslog`, `plugin-json`, test)