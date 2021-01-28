
import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "simple.plugin"
ThisBuild / organizationName := "foobar"

lazy val root = (project in file("."))
  .settings(
    name := "plugin-simple-plugin",
    libraryDependencies += streamy
  )
