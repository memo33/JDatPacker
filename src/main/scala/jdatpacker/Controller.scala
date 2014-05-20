package jdatpacker

import java.io.File
import javax.swing._
import scala.swing.event._
import scala.swing._
import resource._
import java.util.Properties
import passera.unsigned._

object Controller {

  val AppName = "JDatPacker"
  val props = new Properties()
  val propsFile = new File(AppName + ".properties")
  val logFile = new File(AppName + "_log.txt")

  val InputProp = "input"
  val OutputProp = "output"
  val MaxDatSizeProp = "maxDatSize"
  def input = props.getProperty(InputProp, "")
  def output = props.getProperty(OutputProp, "")
  def maxDatSize = Option(props.getProperty(MaxDatSizeProp)).map(_.toLong.toUInt).getOrElse {
    val size = UInt(1024 * 1024 * 512) // 500 MiB
    props.setProperty(MaxDatSizeProp, size.toString)
    size
  }

  def invokeLater(block: => Unit): Unit =
    SwingUtilities.invokeLater(new Runnable { def run = block })

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit = {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  def loadProps(): Unit = if (propsFile.exists) {
    managed(new java.io.FileInputStream(propsFile)) acquireAndGet { in =>
      try { props.load(in) } catch {
        case x: java.io.IOException => x.printStackTrace() // ignore
      }
    }
  }

  def main(args: Array[String]): Unit = invokeLater {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      def uncaughtException(t: Thread, e: Throwable): Unit = {
        Console.err.println("Uncaught exception in thread " + t)
        e.printStackTrace()
        View.showException(e.getMessage, e)
      }
    })
    loadProps()
    val view = new View(input, output)
    view.title = AppName

    view.listenTo(view.startButton, view.browseButtons(0), view.browseButtons(1))
    view.reactions += {
      case ButtonClicked(b) if b == view.startButton => {
        val source = new File(view.textFields(0).text)
        val target = new File(view.textFields(1).text)

        def start() = {
          val dialog = new ProgressDialog(view)
          val worker = new SwingWorker[Iterator[(File, Exception)], (Int, String)] {
            override def doInBackground = Model.pack(source, target, maxDatSize)(publish(_))
            override def done = {
              if (isCancelled) {
                dialog.dispose()
              } else {
                props.setProperty(InputProp, source.getAbsolutePath)
                props.setProperty(OutputProp, target.getAbsolutePath)
                try for (out <- managed(new java.io.FileOutputStream(propsFile))) {
                  props.store(out, "configuration for " + AppName)
                } catch {
                  case x: java.io.FileNotFoundException =>
                    View.showException(e = x, msg =
                      s"File ${propsFile.getAbsolutePath} cannot be accessed.\n" +
                      "You may wish to check your directory permissions. Otherwise, your settings cannot be stored.")
                  case x: java.io.IOException =>
                    View.showException("Could not store settings.", x)
                }
                val errors = get()
                if (errors.nonEmpty) {
                  printToFile(logFile) { p =>
                    errors foreach { case (f, x) =>
                      p.println("FILE IGNORED: " + f)
                      p.println("REASON:       " + x.getMessage)
                      p.println()
                    }
                  }
                  Dialog.showMessage(message = "Datpacking completed with errors. See log file: " + logFile.getAbsolutePath)
                  System.exit(0) // other values cause problems when double-clicking jar file on mac
                } else {
                  Dialog.showMessage(message = "Datpacking completed.")
                  System.exit(0)
                }
              }
            }
            override def process(chunks: java.util.List[(Int, String)]) = {
              println(chunks)
              val (prog, text) = chunks.get(chunks.size - 1)
              dialog.progressBar.value = prog
              dialog.noteLabel.text = text
            }
          }
          worker.execute()

          dialog.listenTo(dialog.cancelButton)
          dialog.reactions += { case ButtonClicked(_) => worker.cancel(true) }
          dialog.peer.setLocationRelativeTo(null)
          dialog.peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
          dialog.visible = true
        }

        if (!source.exists) {
          Dialog.showMessage(message = "The source directory does not exist.")
        } else if (!target.exists) {
          val res = Dialog.showConfirmation(message = "The target directory does not exist. Create directory?")
          if (res == Dialog.Result.Yes) {
            target.mkdirs()
            start()
          }
        } else {
          start()
        }
      }
      case ButtonClicked(b) if view.browseButtons.contains(b) => {
        val i = view.browseButtons.indexOf(b)
        val text = view.textFields(i).text
        val dir = new File(text).getParentFile
        val chooser = if (dir != null && dir.exists) new FileChooser(dir) else new FileChooser
        chooser.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
        val result = chooser.showOpenDialog(null)
        if (result == FileChooser.Result.Approve) {
          view.textFields(i).text = chooser.selectedFile.getAbsolutePath
        }
      }
    }

    view.peer.setLocationRelativeTo(null)
    view.visible = true
  }
}
