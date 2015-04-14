package org.narrativeandplay.hypedyn.dialogs

import org.fxmisc.richtext.StyleClassedTextArea
import org.narrativeandplay.hypedyn.story.Node
import org.tbee.javafx.scene.layout.MigPane

import scalafx.Includes._
import scalafx.scene.control.{Label, TextField, ButtonType, Dialog}
import scalafx.stage.Modality

class NodeEditor(dialogTitle: String, nodeToEdit: Option[Node]) extends Dialog[Node] {
  def this(dialogTitle: String) = this(dialogTitle, None)
  def this(dialogTitle: String, nodeToEdit: Node) = {
    this(dialogTitle, Some(nodeToEdit))
    nodeNameField.text = nodeToEdit.name
    nodeContentField.replaceText(nodeToEdit.content)
  }

  title = dialogTitle
  headerText = None
  resizable = true

  initModality(Modality.APPLICATION_MODAL)

  dialogPane.value.getButtonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  private val nodeNameField = new TextField()
  private val nodeContentField = new StyleClassedTextArea() {
    setWrapText(true)
  }

  private val contentPane = new MigPane("fill") {
    add(new Label("Name"), "grow 0 0, wrap")
    add(nodeNameField, "growx 100, wrap")
    add(new Label("Content"), "grow 0 0, wrap")
    add(nodeContentField, "grow 100 100, wrap")
  }
  dialogPane.value.setContent(contentPane)

  resultConverter = {
    case ButtonType.OK =>
      new Node {
        override def name: String = nodeNameField.text.value

        override def content: String = nodeContentField.getText

        override def id: Long = nodeToEdit map (_.id) getOrElse -1
      }

    case _ => null
  }

  def showAndWait(): Option[Node] = {
    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
