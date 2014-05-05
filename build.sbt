import AssemblyKeys._

name := "JDatPacker"

version := "0.1.0"

scalaVersion := "2.11.0"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  //"-Yinline-warnings",
  //"-optimize",
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

libraryDependencies += "scdbpf" %% "scdbpf" % "0.1.1" from "https://dl.dropboxusercontent.com/s/p5rlbjbcveo2kmh/scdbpf_2.11-0.1.1.jar"

libraryDependencies += "com.jsuereth" %% "scala-arm" % "1.4"

libraryDependencies += "passera.unsigned" %% "scala-unsigned" % "0.1.1" from "https://dl.dropboxusercontent.com/s/yojvk2bb7o1c627/scala-unsigned_2.11-0.1.1.jar"

libraryDependencies += "com.propensive" %% "rapture-core" % "0.9.0"

libraryDependencies += "com.propensive" %% "rapture-io" % "0.9.1"
