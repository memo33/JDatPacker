package jdatpacker

import scala.collection.mutable.{HashSet, Buffer}
import scala.annotation.tailrec
import java.io.File
import io.github.memo33.passera.unsigned.*
import io.github.memo33.scdbpf.*

import io.github.memo33.scdbpf.strategy.throwExceptions

object Model {

  private def hasDbpfExtension(file: File): Boolean = {
    val idx = file.getName.lastIndexOf('.')
    if (idx == -1) {
      false
    } else {
      val ext = file.getName.substring(idx + 1).toLowerCase
      ext == "dat" || ext == "sc4model" || ext == "sc4lot" || ext == "sc4desc"
    }
  }

  private val dbpfFileFilter = new java.io.FileFilter {
    def accept(f: File) = !f.isDirectory && hasDbpfExtension(f)
  }

  private val dirFilter = new java.io.FileFilter {
    def accept(f: File) = f.isDirectory
  }

  private def iterateTree(file: File): Iterator[File] = {
    if (file.isDirectory) {
      val dirChildren = file.listFiles(dirFilter)
      val sortedDirs = dirChildren.sortBy(_.getName.toUpperCase).reverse

      val fileChildren = file.listFiles(dbpfFileFilter)
      val sortedFiles = fileChildren.sortBy(_.getName.toUpperCase).reverse

      sortedDirs.iterator.flatMap(iterateTree(_)) ++ sortedFiles
    } else {
      Iterator(file)
    }
  }

  /** @param files the list of source files to be packed in ''reverse'' loading
    * order. All of these should be DBPF files.
    */
  private def iterateEntries(files: Iterator[File], errors: Buffer[(File, Exception)]): Iterator[StreamedEntry] = {
    val seen = HashSet.empty[Tgi]
    files.flatMap { f =>
//      println(f)
      try {
        DbpfFile.read(f).entries.iterator.filter(e => seen.add(e.tgi))
      } catch {
        case x: java.io.IOException =>
          errors += ((f, x))
          Iterator.empty
      }
    }
  }

  @tailrec
  private def writeEntries(targetDir: File, name: String, count: Int, entryList: Iterator[StreamedEntry], maxDatSize: UInt): Unit = {
    if (entryList.nonEmpty) {
      def fmtName(count: Int) = f"${name}_${count}%03d.dat"
      if (count == 1) {  // rename previous file
        java.nio.file.Files.move(
          new File(targetDir, s"${name}.dat").toPath(),
          new File(targetDir, fmtName(count-1)).toPath(),
          java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        ()
      }
      val target = new File(targetDir, if (count == 0) s"${name}.dat" else fmtName(count))
      var sum = UInt(0)
      val (aIt, bIt) = entryList.span { e => sum += e.size; sum < maxDatSize }
      DbpfFile.write(aIt, target)
      writeEntries(targetDir, name, count + 1, bIt, maxDatSize)
    }
  }

  def pack(sourceDir: File, targetDir: File, maxDatSize: UInt)(publish: ((Int, String)) => Unit): Iterator[(File, Exception)] = {
    val errors = Buffer.empty[(File, Exception)]
    val dirs = sourceDir.listFiles(dirFilter)
    for ((d, i) <- dirs.zipWithIndex) {
      publish(((100.0 * i / dirs.length).toInt, d.getName))
      val entryList = iterateEntries(iterateTree(d), errors)
      writeEntries(targetDir, d.getName, 0, entryList, maxDatSize)
    }
    publish((100, ""))
    errors.iterator
  }
}
