package org.narrativeandplay.hypedyn.dialogs

import java.util.function.Function
import javafx.event.EventHandler
import javafx.scene.control.{TableCell => JfxTableCell, IndexRange => JfxIndexRange}
import javafx.scene.input.{KeyCode => JfxKeyCode, KeyEvent => JfxKeyEvent}

import scala.language.reflectiveCalls

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Pos, Insets, Orientation}
import scalafx.scene.control._
import scalafx.scene.layout._
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
import org.narrativeandplay.hypedyn.uicomponents.Sidebar.SidebarButton
import org.narrativeandplay.hypedyn.utils.CollapsibleSplitPane
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
  val startNodeCheckbox = new CheckBox("Start node") {
    allowIndeterminate = false

    selected <==> node.isStartNodeProperty
    disable <== selected
  }
  val textRulesTable = new TableView[UiNodeContent.UiRuleset] {
    val tableWidth = width

    val rulesetColumn = new TableColumn[UiNodeContent.UiRuleset, UiNodeContent.UiRuleset]("RulesetName") {
      cellValueFactory = { v => ObjectProperty(v.value) }

      cellFactory = { _ =>
        new JfxTableCell[UiNodeContent.UiRuleset, UiNodeContent.UiRuleset] {
          override def startEdit(): Unit = {
            super.startEdit()

            val nameField = new TextField {
              text = itemProperty().get().name

              onKeyReleased = new EventHandler[JfxKeyEvent] {
                override def handle(event: JfxKeyEvent): Unit = event.getCode match {
                  case JfxKeyCode.ENTER =>
                    if (!text().trim.isEmpty) {
                      itemProperty().get.nameProperty() = text()
                      commitEdit(itemProperty().get())
                    }
                    else {
                      cancelEdit()
                    }
                  case JfxKeyCode.ESCAPE => cancelEdit()
                  case _ =>
                }
              }

              focused onChange { (_, _, isFocused) =>
                if (!isFocused) {
                  itemProperty().get.nameProperty() = text()
                  commitEdit(itemProperty().get())
                }
              }
            }

            setText("")
            setGraphic(nameField)
          }

          override def commitEdit(newValue: UiRuleset): Unit = {
            super.commitEdit(newValue)

            setText(newValue.name)
            setGraphic(null)
          }

          override def cancelEdit(): Unit = {
            super.cancelEdit()

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

      // Sets the width of this table column to fill the rest of the width
      // of the table view. The width of the remove button column is 30, and 32 adds a couple of pixels
      // so that the scroll bar at the bottom of the view doesn't show up
      prefWidth <== tableWidth - 32
    }
    val removeButtonColumn = new TableColumn[UiNodeContent.UiRuleset, UiNodeContent.UiRuleset]("Delete") {
      minWidth = 30
      maxWidth = 30
      cellValueFactory = { v => ObjectProperty(v.value) }

      cellFactory = { _ =>
        new JfxTableCell[UiNodeContent.UiRuleset, UiNodeContent.UiRuleset] {
          val removeButton = new Button("-")

          override def updateItem(item: UiNodeContent.UiRuleset, empty: Boolean): Unit = {
            super.updateItem(item, empty)

            if (!empty && item != null) {
              removeButton.onAction = { _ =>
                node.contentProperty().rulesetsProperty() -= item
                nodeContentText.setStyle(item.indexes.startIndex.index.toInt,
                                         item.indexes.endIndex.index.toInt,
                                         new LinkStyleInfo())
              }

              setGraphic(removeButton)
            }
            else {
              setGraphic(null)
            }
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

    // Hides the header of the table
    width onChange { (_, _, _) =>
      val header = lookup("TableHeaderRow").delegate.asInstanceOf[javafx.scene.layout.Pane]
      if (header.visible()) {
        header.setMaxHeight(0)
        header.setMinHeight(0)
        header.setPrefHeight(0)
        header.setVisible(false)
      }
    }

    columns += rulesetColumn
    columns += removeButtonColumn

    items <== node.contentProperty().rulesetsProperty
    editable = true
    placeholder = new Label("")
  }
  lazy val nodeContentText = new InlineStyleTextArea[NodeEditor.LinkStyleInfo](
    new NodeEditor.LinkStyleInfo(),
    new Function[NodeEditor.LinkStyleInfo, String] {
      override def apply(t: LinkStyleInfo): String = t.css
    }) {
    setWrapText(true)
    replaceText(node.content.text)
    node.content.rulesetsProperty() foreach { ruleset =>
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

    def styleSpansAt(indexRange: IndexRange) = {
      val spans = ObservableBuffer.empty[StyleSpan[NodeEditor.LinkStyleInfo]]
      getStyleSpans(indexRange) forEach { styleSpan => spans += styleSpan }
      spans.toList
    }
  }
  lazy val textRulesPane = new RulesPane(conditionDefinitions,
                                         actionDefinitions filter (_.actionLocationTypes contains NodeContentAction),
                                         ObservableBuffer.empty,
                                         story)
  val nodeRulesPane = new RulesPane(conditionDefinitions,
                                    actionDefinitions filter (_.actionLocationTypes contains NodeAction),
                                    node.rulesProperty(),
                                    story)

  val rulesetsListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Label("Text Rules")
      children += new Button("Add text rule") {
        disable <== EasyBind combine (nodeContentText.selectedTextProperty, nodeContentText.selectionProperty, { (s: String, i: JfxIndexRange) =>
          val spansInSelection = nodeContentText styleSpansAt i map (_.getStyle.ruleset)
          val selectionAlreadyContainsRuleset = !(spansInSelection forall (_.isEmpty))
          // Need to manually transform Scala Boolean to java.lang.Boolean because bloody Java<->Scala issues
          Boolean box (s.trim.isEmpty || selectionAlreadyContainsRuleset)
        })

        onAction = { _ =>
          val start = nodeContentText.getSelection.getStart
          val end = nodeContentText.getSelection.getEnd
          val newRuleset = new UiRuleset(firstUnusedRulesetId,
                                         "new rule",
                                         RulesetIndexes(TextIndex(start), TextIndex(end)),
                                         Nil)
          firstUnusedRulesetId = firstUnusedRulesetId.dec
          node.contentProperty().rulesetsProperty() += newRuleset
          nodeContentText.setStyle(start,
                                   end,
                                   new LinkStyleInfo(Some(newRuleset)))
        }
      }
    }
    children += textRulesTable

    VBox.setVgrow(textRulesTable, Priority.Always)
  }
  val contentTextAndRulesetsListPane = new CollapsibleSplitPane {
    orientation = Orientation.HORIZONTAL
    add(rulesetsListVBox)
    add(new VBox {
      children += new Label("Content:") {
        padding = Insets(5)
      }
      children += nodeContentText

      VBox.setVgrow(nodeContentText, Priority.Always)
    })

    dividerPositions = 0.3
  }
  val textRulesVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Label("Text Rules")
      children += new Button("Add rule") {
        disable <== textRulesTable.selectionModel().selectedItemProperty().isNull
        onAction = { _ => textRulesPane.addRule() }
      }
    }
    children += textRulesPane
    VBox.setVgrow(textRulesPane, Priority.Always)
  }
  val nodeRulesVBox = new VBox {
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
  val textAndNodeRulesPane = new CollapsibleSplitPane {
    orientation = Orientation.HORIZONTAL
    add(textRulesVBox)

    add(nodeRulesVBox)

    dividerPositions = 0.5
  }

  val mainContentPane = new CollapsibleSplitPane {
    orientation = Orientation.VERTICAL
    VBox.setVgrow(this, Priority.Always)

    add(contentTextAndRulesetsListPane)

    add(textAndNodeRulesPane)

    dividerPositions = 0.75
  }

  val contentPane = new BorderPane() {
    center = new VBox() {
      children += new Label("Name:")
      children += new HBox(10) {
        padding = Insets(5, 0, 5, 0)
        alignment = Pos.CenterLeft

        children += nodeNameField
        children += startNodeCheckbox

        HBox.setHgrow(nodeNameField, Priority.Always)
      }
      children += mainContentPane
    }

    bottom = new ToolBar {
      style = "-fx-background-color: transparent;"

      // Blank and invisible button to push the next 2 buttons into a nice position
      items += new Button("") {
        minWidth = 35
        visible = false
      }
      items += new Button("Text Rules") {
        onAction = { _ =>
          (textAndNodeRulesPane isShown textRulesVBox, mainContentPane isShown textAndNodeRulesPane) match {
            case (true, true) =>
              if (textAndNodeRulesPane isShown nodeRulesVBox) textAndNodeRulesPane.hide(textRulesVBox) else mainContentPane.hide(textAndNodeRulesPane)
            case (false, true) => textAndNodeRulesPane.show(textRulesVBox)
            case (true, false) => mainContentPane.show(textAndNodeRulesPane)
            case (false, false) =>
              textAndNodeRulesPane.hide(nodeRulesVBox)
              textAndNodeRulesPane.show(textRulesVBox)
              mainContentPane.show(textAndNodeRulesPane)
          }
        }
      }
      items += new Button("Node Rules") {
        onAction = { _ =>
          (textAndNodeRulesPane isShown nodeRulesVBox, mainContentPane isShown textAndNodeRulesPane) match {
            case (true, true) =>
              if (textAndNodeRulesPane isShown textRulesVBox) textAndNodeRulesPane.hide(nodeRulesVBox) else mainContentPane.hide(textAndNodeRulesPane)
            case (false, true) => textAndNodeRulesPane.show(nodeRulesVBox)
            case (true, false) => mainContentPane.show(textAndNodeRulesPane)
            case (false, false) =>
              textAndNodeRulesPane.hide(textRulesVBox)
              textAndNodeRulesPane.show(nodeRulesVBox)
              mainContentPane.show(textAndNodeRulesPane)
          }
        }
      }
    }

    left = new ToolBar {
      style = "-fx-background-color: transparent;"
      orientation = Orientation.VERTICAL

      items += new SidebarButton("Text Rules") {
        onAction = { _ =>
          contentTextAndRulesetsListPane isShown rulesetsListVBox match {
            case true => contentTextAndRulesetsListPane.hide(rulesetsListVBox)
            case false => contentTextAndRulesetsListPane.show(rulesetsListVBox)
          }
        }
      }
    }
  }

  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      node.contentProperty().textProperty() = nodeContentText.getText
      updateNodeContentRulesetsIndexes()
      node
    case _ => null
  }

  onShown = { _ =>
    nodeNameField.requestFocus()
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
