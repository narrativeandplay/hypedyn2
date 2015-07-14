package org.narrativeandplay.hypedyn.dialogs

import javafx.{scene => jfxs}
import javafx.scene.{control => jfxsc}

import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, VBox, HBox, BorderPane}
import scalafx.stage.{Window, Modality}
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.richtext.StyleClassedTextArea
import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.story.NodalContent.RulesetIndexes
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.uicomponents.RulesPane

class NodeEditor private (dialogTitle: String,
                          conditionDefinitions: List[ConditionDefinition],
                          actionDefinitions: List[ActionDefinition],
                          story: Narrative,
                          nodeToEdit: Option[Nodal],
                          ownerWindow: Window) extends Dialog[Nodal] {
  def this(dialogTitle: String,
           conditionDefinitions: List[ConditionDefinition],
           actionDefinitions: List[ActionDefinition],
           story: Narrative,
           ownerWindow: Window) = this(dialogTitle, conditionDefinitions, actionDefinitions, story, None, ownerWindow)

  def this(dialogTitle: String,
           nodeToEdit: Nodal,
           conditionDefinitions: List[ConditionDefinition],
           actionDefinitions: List[ActionDefinition],
           story: Narrative,
           ownerWindow: Window) = {
    this(dialogTitle, conditionDefinitions, actionDefinitions, story, Some(nodeToEdit), ownerWindow)
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
  private val okButton: Node = dialogPane().lookupButton(ButtonType.OK)

  private val nodeNameField = new TextField() {
    text onChange { (_, _, name) =>
      okButton.disable = name.trim().isEmpty
    }
  }
  private val nodeContentField = new StyleClassedTextArea() {
    setWrapText(true)
  }

  private val contentPane = new VBox() {
    children += new Label("Name: ")
    children += nodeNameField
    children += new Label("Content: ")
    children += nodeContentField
    children += new RulesPane(conditionDefinitions, actionDefinitions, List(new UiRule(RuleId(-1), "Hello", And, Nil, Nil),
                                                                            new UiRule(RuleId(-1), "Hello2", Or, Nil, Nil)))
  }
  VBox.setVgrow(nodeContentField, Priority.Always)
  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      UiNode(nodeToEdit map (_.id) getOrElse NodeId(-1),
             nodeNameField.text.value,
             UiNodeContent(nodeContentField.getText, Map.empty), // The map must be replaced with the actual map of rules
             initIsStartNode = false,
             Nil) // Must be replaced with the actual rules

    case _ => null
  }

  def showAndWait(): Option[Nodal] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
