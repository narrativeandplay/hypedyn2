package org.narrativeandplay.hypedyn.dialogs

import java.util.function.Function
import javafx.event.EventHandler
import javafx.scene.control.{ListCell => JfxListCell}
import javafx.scene.input
import javafx.scene.input.KeyCode
import scala.language.reflectiveCalls

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Pos, Insets, Orientation}
import scalafx.scene.control._
import scalafx.scene.layout.{StackPane, Priority, HBox, VBox}
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.easybind.EasyBind
import org.fxmisc.richtext.{StyleSpan, InlineStyleTextArea}

import org.narrativeandplay.hypedyn.dialogs.NodeEditor.LinkStyleInfo
import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetId, TextIndex, RulesetIndexes}
import org.narrativeandplay.hypedyn.story.UiNodeContent.UiRuleset
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.InterfaceToUiImplementation._
import org.narrativeandplay.hypedyn.uicomponents.RulesPane
import org.narrativeandplay.hypedyn.utils.ScalaJavaImplicits._

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

  private var firstUnusedRulesetId = RulesetId(-1)

  title = dialogTitle
  headerText = None
  resizable = true

  initOwner(ownerWindow)
  initModality(Modality.NONE)

  width = 640
  height = 480

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val story: ObjectProperty[UiStory] = ObjectProperty(narrative)
  val node: UiNode = nodeToEdit getOrElse new UiNode(NodeId(-1), "New Node", new UiNodeContent("", Nil), false, Nil)

  val nodeNameField = new TextField() {
    text <==> node.nameProperty
  }
  val textRulesList = new ListView[UiNodeContent.UiRuleset]() {
    cellFactory = { _ =>
      new JfxListCell[UiNodeContent.UiRuleset] {
        override def startEdit(): Unit = {
          super.startEdit()

          setText("")
          setGraphic(new TextField {
            text = itemProperty().get().name

            onKeyReleased = new EventHandler[input.KeyEvent] {
              override def handle(event: input.KeyEvent): Unit = event.getCode match {
                case KeyCode.ENTER =>
                  if (!text().trim.isEmpty) {
                    itemProperty().get.nameProperty() = text()
                    commitEdit(itemProperty().get())
                  }
                  else {
                    cancelEdit()
                  }
                case KeyCode.ESCAPE => cancelEdit()
                case _ =>
              }
            }
          })
        }

        override def cancelEdit(): Unit = {
          super.cancelEdit()

          setText(itemProperty().get().name)
          setGraphic(null)
        }

        override def commitEdit(newValue: UiRuleset): Unit = {
          super.commitEdit(newValue)

          setText(itemProperty().get().name)
          setGraphic(null)
        }

        override def updateItem(item: UiRuleset, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
          else {
            setText("")
          }
        }
      }
    }

    selectionModel().selectedItemProperty onChange { (_, _, `new`) =>
      Option(`new`) match {
        case Some(ruleset) =>
          nodeContentText.selectRange(ruleset.indexes.startIndex.index.toInt, ruleset.indexes.endIndex.index.toInt)
          textRulesPane.rules() = ruleset.rulesProperty
        case None =>
          textRulesPane.rules() = ObservableBuffer.empty[UiRule]
      }
    }

    nodeContentText.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    items = node.contentProperty().rulesetsProperty
    editable = true
  }
  lazy val nodeContentText = new InlineStyleTextArea[NodeEditor.LinkStyleInfo](
    new NodeEditor.LinkStyleInfo(),
    new Function[NodeEditor.LinkStyleInfo, String] {
      override def apply(t: LinkStyleInfo): String = t.css
    }) {
    setWrapText(true)
    replaceText(node.content.text)
    node.content.rulesetsProperty foreach { ruleset =>
      setStyle(ruleset.indexes.startIndex.index.toInt,
               ruleset.indexes.endIndex.index.toInt,
               new LinkStyleInfo(Some(ruleset)))
    }

    getUndoManager.forgetHistory() // Ensure that the initialisation of the text done above is not undoable

    beingUpdatedProperty onChange { (_, _, beingUpdated) =>
      if (!beingUpdated) {
        updateNodeContentRulesetsIndexes()
      }
    }

    def styleSpans = {
      val spans = ObservableBuffer.empty[StyleSpan[NodeEditor.LinkStyleInfo]]
      getStyleSpans(0, getText.length) forEach { styleSpan => spans += styleSpan }
      spans.toList
    }
  }
  lazy val textRulesPane = new RulesPane(conditionDefinitions,
                                    actionDefinitions filter (_.actionType contains NodeContentAction),
                                    ObservableBuffer.empty,
                                    story)
  val nodeRulesPane = new RulesPane(conditionDefinitions,
                                    actionDefinitions filter (_.actionType contains NodeAction),
                                    node.rulesProperty,
                                    story)

  val contentPane = new VBox() {
    children += new Label("Name:")
    children += new StackPane {
      padding = Insets(5, 0, 5, 0)
      children += nodeNameField
    }
    children += new SplitPane() {
      orientation = Orientation.VERTICAL
      VBox.setVgrow(this, Priority.Always)

      items += new SplitPane() {
        orientation = Orientation.HORIZONTAL
        items += new VBox {
          children += new HBox {
            padding = Insets(5)
            alignment = Pos.CenterLeft
            children += new Label("Text Rules")
            children += new Button("Add text rule") {
              disable <== EasyBind.map(nodeContentText.selectedTextProperty, { s: String =>
                Boolean box s.trim.isEmpty  // Need to manually transform Scala Boolean to java.lang.Boolean because bloody Java<->Scala issues
              })

              onAction = { _ =>
                val start = nodeContentText.getSelection.getStart
                val end = nodeContentText.getSelection.getEnd
                val newRuleset = new UiRuleset(firstUnusedRulesetId,
                                               "new rule",
                                               RulesetIndexes(TextIndex(start), TextIndex(end)),
                                               Nil)
                firstUnusedRulesetId = firstUnusedRulesetId.dec
                node.contentProperty().rulesetsProperty += newRuleset
                nodeContentText.setStyle(start,
                                         end,
                                         new LinkStyleInfo(Some(newRuleset)))
              }
            }
          }
          children += textRulesList

          VBox.setVgrow(textRulesList, Priority.Always)
        }
        items += new VBox {
          children += new Label("Content:") {
            padding = Insets(5)
          }
          children += nodeContentText

          VBox.setVgrow(nodeContentText, Priority.Always)
        }

        dividerPositions = 0.3
      }

      items += new SplitPane() {
        orientation = Orientation.HORIZONTAL
        items += new VBox {
          children += new HBox {
            padding = Insets(5)
            alignment = Pos.CenterLeft
            children += new Label("Text Rules")
            }
          }
          children += textRulesPane
          VBox.setVgrow(textRulesPane, Priority.Always)
        }

        items += new VBox {
          children += new HBox {
            padding = Insets(5)
            alignment = Pos.CenterLeft
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

  def updateNodeContentRulesetsIndexes(): Unit = {
    import NodalContent._
    val spans = nodeContentText.styleSpans.scanLeft((0, 0, None: Option[UiRuleset])) {
      case ((_, end, rulesetOption), styleSpan) =>
        (end, end + styleSpan.getLength, styleSpan.getStyle.ruleset)
    }
    (spans.tail foldLeft List(spans.head)) {
      case (resList @ (headStart, _, headSpan) :: tail, (start, end, span)) =>
        if (headSpan == span) (headStart, end, span) :: tail else (start, end, span) :: resList
      case (Nil, input) => input :: Nil
    } collect { case (start, end, Some(ruleset)) =>
      ruleset.indexesProperty() = RulesetIndexes(TextIndex(start), TextIndex(end))
    }
  }

  def showAndWait(): Option[Nodal] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}

object NodeEditor {
  class LinkStyleInfo(val ruleset: Option[UiRuleset] = None) {
    private val linkStyle = "-fx-font-weight: bold; -fx-underline: true;"

    def css = if (ruleset.isDefined) linkStyle else ""

    override def toString = s"hasRule: ${ruleset.isDefined}"
  }
}
