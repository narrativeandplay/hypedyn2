package org.narrativeandplay.hypedyn.dialogs

import scalafx.Includes._
import scalafx.scene.control.{Label, TextField, ButtonType, Dialog}
import scalafx.stage.{Window, Modality}

import org.fxmisc.richtext.StyleClassedTextArea
import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.story.{UiNodeContent, UiNode, NodeId, Nodal}

class NodeEditor private (dialogTitle: String, nodeToEdit: Option[Nodal], ownerWindow: Window) extends Dialog[Nodal] {
  def this(dialogTitle: String, ownerWindow: Window) = this(dialogTitle, None, ownerWindow)

  def this(dialogTitle: String, nodeToEdit: Nodal, ownerWindow: Window) = {
    this(dialogTitle, Some(nodeToEdit), ownerWindow)
    nodeNameField.text = nodeToEdit.name
    nodeContentField.replaceText(nodeToEdit.content.text)
    nodeContentField.getUndoManager.forgetHistory()
  }

  title = dialogTitle
  headerText = None
  resizable = true

  initOwner(ownerWindow)
  initModality(Modality.NONE)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

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
  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      UiNode(nodeToEdit map (_.id) getOrElse NodeId(-1),
             nodeNameField.text.value,
             UiNodeContent(nodeContentField.getText, Map.empty), // The map must be replaced with the actual map of rules
             isStartNode = false,
             Nil) // Must be replaced with the actual rules

    case _ => null
  }

  def showAndWait(): Option[Nodal] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
