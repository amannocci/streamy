lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "simple.library",
      scalaVersion := "2.12.4",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "plugin-simple-library",
    libraryDependencies ++= Seq("io.techcode.streamy" %% "streamy-core" % "0.1.0-SNAPSHOT" % Provided)
  )
