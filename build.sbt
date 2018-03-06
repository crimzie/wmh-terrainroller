name := "wmh-terrainroller"
version := "0.2.2"
organization := "com.crimzie"
scalaVersion := "2.12.4"

scalacOptions += "-Ypartial-unification"
mainClass := Some("com.crimzie.wmh.terrain.Server")

val http4sVersion = "0.18.1"
val scribeVersion = "2.2.0"
val monixVersion = "3.0.0-M2"
//val LogbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.specs2" %% "specs2-core" % "4.0.2" % "test",
  "com.outr" %% "scribe" % scribeVersion,
  "com.outr" %% "scribe-slf4j" % scribeVersion,
  "io.monix" %% "monix" % monixVersion,
  //"io.monix" %% "monix-cats" % monixVersion,
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val root = project in file(".") enablePlugins JavaAppPackaging enablePlugins DockerPlugin

dockerBaseImage := "openjdk:jre"
dockerExposedPorts := 8080 :: Nil
dockerRepository := Some("eu.gcr.io/wmh-terrain")
