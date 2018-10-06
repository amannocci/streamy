lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "simple.plugin",
      scalaVersion := "2.12.6",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "plugin-simple-plugin",
    libraryDependencies ++=  Seq("io.techcode.streamy" %% "streamy-core" % "0.1.0-SNAPSHOT" % Provided)
  )
