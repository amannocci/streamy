import sbt._

object Dependencies {

  // Dependencies version
  val streamyVersion = "0.1.0-SNAPSHOT"

  // Settings
  val settings = Seq(
    "io.techcode.streamy" %% "streamy-core"
  ).map(_ % streamyVersion % Provided)
}
