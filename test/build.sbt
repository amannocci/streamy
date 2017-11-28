import Dependencies._

name := name.value + "-test"

// Custom resolvers
resolvers ++= Seq(
  "Techcode" at "https://nexus.techcode.io/repository/maven-public"
)

// Building
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "org.mockito" % "mockito-core" % mockitoVersion
).map(_ % Compile)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit", // Apache 2 License
  "com.typesafe.akka" %% "akka-stream-testkit" // Apache 2 License
).map(_ % akkaVersion % Compile)