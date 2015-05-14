package org.narrativeandplay.hypedyn.dialogs

import scalafx.scene.control.{Label, TextField, ButtonType, Dialog}
import scalafx.stage.Modality

import org.fxmisc.richtext.StyleClassedTextArea
import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.story.{NodeContent, NodeId, Nodal}

class NodeEditor(dialogTitle: String, nodeToEdit: Option[Nodal]) extends Dialog[Nodal] {
  def this(dialogTitle: String) = this(dialogTitle, None)

  def this(dialogTitle: String, nodeToEdit: Nodal) = {
    this(dialogTitle, Some(nodeToEdit))
    nodeNameField.text = nodeToEdit.name
    nodeContentField.replaceText(nodeToEdit.content.text)
    nodeContentField.getUndoManager.forgetHistory()
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
      new Nodal {
        override def name: String = nodeNameField.text.value

        override def content: NodeContent = NodeContent(nodeContentField.getText)

        override def id: NodeId = nodeToEdit map (_.id) getOrElse NodeId(-1)

        /**
         * Determines if this node represents the start of the story
         */
        override def isStartNode: Boolean = false
      }

    case _ => null
  }

  def showAndWait(): Option[Nodal] = {
    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
