import AssemblyKeys._

name := "JDatPacker"

organization := "com.github.memo33"

version := "0.1.4"

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  //"-Yinline-warnings",
  "-optimize",
  "-encoding", "UTF-8",
  "-target:jvm-1.6")

autoAPIMappings := true


zipPath <<= (target, name, version) map { (t: File, n, v) => t / s"${n}-${v}.zip" }

readmePath <<= (baseDirectory) map { (b: File) => b / "README.md" }

licensePath <<= (baseDirectory) map { (b: File) => b / "LICENSE" }

dist <<= (assembly in Compile, readmePath, licensePath, zipPath, streams) map {
  (fatjar: File, readme: File, license: File, out: File, ts: TaskStreams) =>
    val inputs: Seq[(File,String)] = Seq(fatjar, readme, license) x Path.flat
    IO.zip(inputs, out)
    ts.log.info("Created zip archive at " + out.toString)
    out
}


packSettings

packMain := Map(s"${name.value}-${version.value}" -> "jdatpacker.Controller")


assemblySettings

jarName in assembly := s"${name.value}-${version.value}.jar"

mainClass in assembly := Some("jdatpacker.Controller")


libraryDependencies += "org.scala-lang" % "scala-swing" % "2.11.0-M7"


resolvers += "memo33-gdrive-repo" at "https://googledrive.com/host/0B9r6o2oTyY34ZVc4SFBWMV9yb0E/repo/releases/"

libraryDependencies += "com.github.memo33" %% "scdbpf" % "0.1.4"
