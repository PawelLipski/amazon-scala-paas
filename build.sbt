name := "akka-sample-remote-scala"

version := "2.3.10"

scalaVersion := "2.10.4"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-remote_2.10" % "2.3.11",
  "com.typesafe.akka" % "akka-actor_2.10" % "2.3.11"
)

