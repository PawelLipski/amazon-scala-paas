name := "amazon-app-platform"

version := "1.0-SNAPSHOT"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += Resolver.typesafeIvyRepo("releases")

scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  jdbc,
  anorm,
  cache,
  "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3",
  "com.jsuereth" %% "scala-arm" % "1.4"
  //"com.geekcap.informit.akka" % "akka-messages" % "1.0-SNAPSHOT"
)
     
play.Project.playScalaSettings

