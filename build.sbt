name := "JDatPacker"

organization := "io.github.memo33"

version := "0.1.5-SNAPSHOT"

scalaVersion := "2.11.12"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  //"-Yinline-warnings",
  "-optimize",
  "-encoding", "UTF-8",
  "-target:jvm-1.6")

autoAPIMappings := true

lazy val zipPath = TaskKey[File]("zip-path", "path to dist zip file")
zipPath := target.value / s"${name.value}-${version.value}.zip"

// create a distributable zip file with `sbt dist` (containing the large jar)
lazy val dist = TaskKey[File]("dist", "creates a distributable zip file")
dist := {
  val fatjar: File = (Compile / assembly).value
  val inputs: Seq[(File, String)] = Seq(fatjar, (baseDirectory.value / "README.md"), (baseDirectory.value / "LICENSE")) pair Path.flat
  IO.zip(inputs, zipPath.value, time = None)
  streams.value.log.info("Created zip archive at " + zipPath.value.toString)
  zipPath.value
}


assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

assembly / mainClass := Some("jdatpacker.Controller")


libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

libraryDependencies += "io.github.memo33" %% "scdbpf" % "0.2.0"
