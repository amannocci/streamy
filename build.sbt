import sbt.Keys.{libraryDependencies, publishTo, scalacOptions}

lazy val scalaTestVersion = "3.0.1"

lazy val commonSettings = Seq(
  name := "streamy",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % scalaTestVersion % "test"),
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
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

  // Scala compiler options
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  )
)

lazy val core = (project in file("core"))
  .settings(commonSettings)

lazy val `plugin-fingerprint` = (project in file("plugin-fingerprint"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val `plugin-syslog` = (project in file("plugin-syslog"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val `plugin-json` = (project in file("plugin-json"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val root = (project in file("."))
  .settings(Seq(publish := {}))
  .aggregate(core, `plugin-fingerprint`, `plugin-syslog`, `plugin-json`)