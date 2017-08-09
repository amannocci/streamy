import sbt.Keys.libraryDependencies

lazy val scalaTestVersion = "3.0.1"

lazy val commonSettings = Seq(
  name := "streamy",
  version := "0.1.0",
  scalaVersion := "2.12.2",
  libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % scalaTestVersion % "test")
)

lazy val core = (project in file("core"))
  .settings(commonSettings)

lazy val `plugin-syslog` = (project in file("plugin-syslog"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val `plugin-json` = (project in file("plugin-json"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, `plugin-syslog`, `plugin-json`)