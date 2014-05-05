import sbt._
import java.io.File

object JDatPackerDef extends Build {

  val readmePath = TaskKey[File]("readme-path", "path to readme file")
  val licensePath = TaskKey[File]("license-path", "path to license file")
  val zipPath = TaskKey[File]("zip-path", "path to dist zip file")
  val dist = TaskKey[File]("dist", "creates a distributable zip file")
}
