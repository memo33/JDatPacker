name := "JDatPacker"

organization := "io.github.memo33"

version := "0.2.1"

description := "Cross-platform utility for repackaging SimCity 4 plugin files"

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

scalaVersion := "3.3.1"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-encoding", "UTF-8",
  // "-opt:l:inline", "-opt-inline-from:<sources>",
  "-Wvalue-discard",
  "-source:future",
  "-release:8")

javacOptions ++= Seq("--release", "8")

autoAPIMappings := true

lazy val zipPath = TaskKey[File]("zip-path", "path to dist zip file")
zipPath := target.value / s"${name.value}-${version.value}.zip"

// create a distributable zip file with `sbt dist` (containing the large jar)
lazy val dist = TaskKey[File]("dist", "creates a distributable zip file")
dist := {
  val fatjar: File = (Compile / assembly).value
  val inputs: Seq[(File, String)] =
    Seq(
      fatjar,
      (baseDirectory.value / "README.md"),
      (baseDirectory.value / "LICENSE"),
      (baseDirectory.value / "launch-JDatPacker-Linux.sh"),
      (baseDirectory.value / "launch-JDatPacker-macOS.command"),
      (baseDirectory.value / "launch-JDatPacker-Windows.bat"),
    ) pair Path.flat
  // IO.zip(inputs, zipPath.value, time = None)
  // We use an external `zip` command in order to preserve file permissions (executable bit)
  zipPath.value.delete()
  import scala.sys.process._
  (Seq("zip", "-j", zipPath.value.toString) ++ inputs.map(_._1.toString)).!
  streams.value.log.info("Created zip archive at " + zipPath.value.toString)
  zipPath.value
}


assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

assembly / mainClass := Some("jdatpacker.Controller")


libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

libraryDependencies += "io.github.memo33" %% "scdbpf" % "0.2.1" cross CrossVersion.for3Use2_13
