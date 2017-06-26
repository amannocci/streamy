lazy val commonSettings = Seq(
  name := "streamy",
  version := "0.1.0",
  scalaVersion := "2.12.2"
)

lazy val core = (project in file("streamy-core"))
  .settings(commonSettings)

lazy val root = (project in file("."))
  .aggregate(core)