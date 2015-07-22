package org.narrativeandplay.hypedyn.dialogs

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Orientation
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, HBox, VBox}
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.richtext.StyleClassedTextArea

import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinition, ConditionDefinition}
import org.narrativeandplay.hypedyn.story.InterfaceToUiImplementation._
import org.narrativeandplay.hypedyn.uicomponents.RulesPane

class NodeEditor private (dialogTitle: String,
                          conditionDefinitions: List[ConditionDefinition],
                          actionDefinitions: List[ActionDefinition],
                          narrative: Narrative,
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
           ownerWindow: Window) =
    this(dialogTitle, conditionDefinitions, actionDefinitions, story, Some(nodeToEdit), ownerWindow)

  title = dialogTitle
  headerText = None
  resizable = true

  initOwner(ownerWindow)
  initModality(Modality.NONE)

  width = 640
  height = 480

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val story: ObjectProperty[UiStory] = ObjectProperty(narrative)
  val node: UiNode = nodeToEdit getOrElse new UiNode(NodeId(-1), "New Node", new UiNodeContent("", Map.empty), false, Nil)

  def initEdit(): Unit = {}

  val nodeNameField = new TextField() {
    text <==> node.nameProperty
  }
  val textRulesList = new ListView[UiRule]() {
    prefHeight = 200
    prefWidth = 150
  }
  val nodeContentText = new StyleClassedTextArea() {
    setWrapText(true)
    replaceText(node.content.text)
    getUndoManager.forgetHistory() // Ensure that the initialisation of the text done above is not undoable
  }
  val textRulesPane = new RulesPane(conditionDefinitions, actionDefinitions, ObservableBuffer.empty, story)
  val nodeRulesPane = new RulesPane(conditionDefinitions, actionDefinitions, node.rulesProperty, story)

  val contentPane = new VBox() {
    children += new Label("Name:")
    children += nodeNameField
    children += new SplitPane() {
      orientation = Orientation.VERTICAL
      VBox.setVgrow(this, Priority.Always)

      items += new SplitPane() {
        orientation = Orientation.HORIZONTAL
        items += new VBox {
          children += new Label("Text Rules")
          children += textRulesList

          VBox.setVgrow(textRulesList, Priority.Always)
        }
        items += new VBox {
          children += new Label("Content:")
          children += nodeContentText

          VBox.setVgrow(nodeContentText, Priority.Always)
        }

        dividerPositions = 0.3
      }

      items += new SplitPane() {
        orientation = Orientation.HORIZONTAL
        items += new VBox {
          children += new HBox {
            children += new Label("Text Rules")
            children += new Button("Add text rule")
          }
          children += textRulesPane
          VBox.setVgrow(textRulesPane, Priority.Always)
        }

        items += new VBox {
          children += new HBox {
            children += new Label("Node Rules")
            children += new Button("Add node rule") {
              onAction = { _ => nodeRulesPane.addRule() }
            }
          }
          children += nodeRulesPane

          VBox.setVgrow(nodeRulesPane, Priority.Always)
        }

        dividerPositions = 0.5
      }

      dividerPositions = 0.75
    }
  }

  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      node.contentProperty().textProperty() = nodeContentText.getText
      node
    case _ => null
  }

  def showAndWait(): Option[Nodal] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}

object NodeEditor {
  class LinkStyleInfo(val rule: Option[UiRule] = None) {
    private val linkStyle = "-fx-font-weight: bold; -fx-underline: true;"

    def css = if (rule.isDefined) linkStyle else ""

    override def toString = s"hasRule: ${rule.isDefined}"
  }
}
