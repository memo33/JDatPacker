package jdatpacker

import scala.swing._
import javax.swing.JOptionPane, JOptionPane._
import resource._
import java.io._

class View(inputPath: String, outputPath: String) extends MainFrame {
  val startButton = new Button("Start")
  defaultButton = startButton
  val browseButtons = Seq.fill(2)(new Button("Browse"))
  browseButtons(0).tooltip = "Select the directory that contains the folders to datpack (usually outside your plugins)"
  browseButtons(1).tooltip = "Select the destination directory for the datpacked files (usually inside your plugins)"
  val textFields = Seq.fill(2)(new TextField(36))
  textFields(0).text = inputPath
  textFields(1).text = outputPath
  val labels = Seq("Source Directory", "Target Directory") map (new Label(_))

  contents = new GridBagPanel {
    val c = new Constraints()
    c.insets = new Insets(3,3,3,3)
    c.fill = GridBagPanel.Fill.Both
    for (y <- 0 to 1) {
      c.weightx = 0.0; c.weighty = 0.0; c.gridx = 0; c.gridy = y
      add(labels(y), c)
      c.weightx = 1.0; c.gridx = 1
      add(textFields(y), c)
      c.weightx = 0.0; c.gridx = 2
      add(browseButtons(y), c)
    }
    val wrapper = new BorderPanel { add(startButton, BorderPanel.Position.East) }
    c.gridx = 1; c.gridy = 2
    add(wrapper, c)
    border = javax.swing.BorderFactory.createEmptyBorder(5,5,5,5)
  }
}

object View {
  private val Width = 400
  private def wrapLabelText(msg: String, width: Int): String = s"<html><body><p style='width: ${width}px;'>${msg}</body></html>"

  def showException(msg: String, e: Throwable): Unit = {
    val p = new BorderPanel {
      add(new Label(wrapLabelText(msg, Width)), BorderPanel.Position.North)
      for (sw <- managed(new StringWriter())) {
        managed(new PrintWriter(sw)) acquireFor e.printStackTrace
        val ta = new TextArea(sw.toString)
        ta.editable = false
        add(new ScrollPane(ta), BorderPanel.Position.Center)
      }
      preferredSize = new java.awt.Dimension(600, 300)
    }
    Dialog.showMessage(message = p.peer, messageType = Dialog.Message.Error)
  }
}

class ProgressDialog(owner: Window) extends Dialog(owner) {
  title = "Running"
  val cancelButton = new Button("Cancel")
  val noteLabel = new Label()
  val progressBar = new ProgressBar()
  val panel = new GridPanel(3, 1) {
    contents += new Label("Processing files…")
    contents += noteLabel
    contents += progressBar
    preferredSize = new java.awt.Dimension(300, 0)
  }
  contents = Component.wrap(new JOptionPane(panel.peer, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, Array(cancelButton.peer)))
}
