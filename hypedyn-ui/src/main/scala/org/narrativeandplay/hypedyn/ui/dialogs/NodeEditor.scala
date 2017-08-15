package org.narrativeandplay.hypedyn.ui.dialogs

import java.io.{DataInputStream, DataOutputStream}
import java.util
import java.util.function.BiConsumer
import javafx.collections.ObservableList
import javafx.{event => jfxe}
import javafx.event.{EventHandler, ActionEvent => JfxActionEvent}
import javafx.scene.control.{IndexRange => JfxIndexRange}
import javafx.scene.{input => jfxsi}
import javafx.scene.input.{KeyEvent => JfxKeyEvent}
import javafx.scene.text.{Text, TextFlow}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.{ActionEvent, Event}
import scalafx.geometry.{Insets, Orientation, Pos}
import scalafx.scene.control._
import scalafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.layout._
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control.Tab.sfxTab2jfx

import org.fxmisc.easybind.EasyBind
import org.fxmisc.richtext.model.{Codec, StyleSpan, StyledText}
import org.fxmisc.richtext.StyledTextArea

import org.narrativeandplay.hypedyn.api.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.ui.dialogs.NodeEditor.{LinkStyleInfo, NodeContentTextArea}
import org.narrativeandplay.hypedyn.ui.events.UiEventDispatcher
import org.narrativeandplay.hypedyn.api.story.NodalContent.{RulesetId, RulesetIndexes, TextIndex}
import org.narrativeandplay.hypedyn.ui.story.UiNodeContent.UiRuleset
import org.narrativeandplay.hypedyn.ui.story._
import org.narrativeandplay.hypedyn.api.story.rules.ActionLocationType.{NodeAction, NodeContentAction}
import org.narrativeandplay.hypedyn.api.story.rules.{ActionDefinition, ConditionDefinition, RuleId}
import org.narrativeandplay.hypedyn.core.story.rules._
import org.narrativeandplay.hypedyn.ui.story.InterfaceToUiImplementation._
import org.narrativeandplay.hypedyn.ui.uicomponents.RulesPane
import org.narrativeandplay.hypedyn.ui.uicomponents.Sidebar.SidebarButton
import org.narrativeandplay.hypedyn.ui.utils.{CollapsibleSplitPane, ExpandableEmptySpace}
import org.narrativeandplay.hypedyn.api.utils.Scala2JavaFunctionConversions._

/**
 * Dialog for editing nodes
 *
 * @param dialogTitle The title of the dialog
 * @param conditionDefinitions The list of condition definitions
 * @param actionDefinitions The list of action defintions
 * @param narrative The story that the node that is being edited belongs to
 * @param nodeToEdit An option containing the node to edit, or None if a new node is to be created
 * @param ownerWindow The parent window of the dialog, to inherit icons
 */
class NodeEditor private (dialogTitle: String,
                          conditionDefinitions: List[ConditionDefinition],
                          actionDefinitions: List[ActionDefinition],
                          narrative: Narrative,
                          nodeToEdit: Option[Nodal],
                          ownerWindow: Window) extends Dialog[Nodal] {
  /**
   * Creates a new node editor for creating a new node
   *
   * @param dialogTitle The title of the dialog
   * @param conditionDefinitions The list of condition definitions
   * @param actionDefinitions The list of action definitions
   * @param story The story that the node that is being created belongs to
   * @param ownerWindow The parent window of the dialog, to inherit icons
   * @return A new node editor
   */
  def this(dialogTitle: String,
           conditionDefinitions: List[ConditionDefinition],
           actionDefinitions: List[ActionDefinition],
           story: Narrative,
           ownerWindow: Window) = this(dialogTitle, conditionDefinitions, actionDefinitions, story, None, ownerWindow)

  /**
   * Creates a new node editor for editing a node
   *
   * @param dialogTitle The title of the dialog
   * @param nodeToEdit The node to edit
   * @param conditionDefinitions The list of condition definitions
   * @param actionDefinitions The list of action definitions
   * @param story The story that the node that is being edited belongs to
   * @param ownerWindow The parent window of the dialog, to inherit icons
   * @return A new node editor
   */
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
  initModality(Modality.None)

  dialogPane().setPrefSize(1280, 800)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Apply, ButtonType.Cancel)
  private[this] val okButton: Button = dialogPane().lookupButton(ButtonType.OK).asInstanceOf[javafx.scene.control.Button]
  private[this] val applyButton: Button = dialogPane().lookupButton(ButtonType.Apply).asInstanceOf[javafx.scene.control.Button]

  val story: ObjectProperty[UiStory] = ObjectProperty(narrative)
  private val node: ObjectProperty[UiNode] = ObjectProperty(nodeToEdit getOrElse NodeEditor.newNode)
  private[this] val monadicNode = EasyBind monadic node

  private var updateFunc = UiEventDispatcher.updateNode(nodeToEdit getOrElse NodeEditor.newNode)

  story onChange { (_, _, newStory) =>
    newStory.nodes find (_.id == node().id) foreach updateNodeData
  }

  okButton.addEventFilter(ActionEvent.Any, { ae: JfxActionEvent =>
    node().contentProperty().textProperty() = nodeContentText.getText
    updateNodeContentRulesetsIndexes()

    if (node().id.isValid) {
      updateFunc(node())
    }
    else {
      UiEventDispatcher.createNode(node())
    }

    result = node()
    ae.consume()
  })
  applyButton.addEventFilter(ActionEvent.Any, { ae: JfxActionEvent =>
    node().contentProperty().textProperty() = nodeContentText.getText
    updateNodeContentRulesetsIndexes()

    if (node().id.isValid) {
      updateFunc(node())
    }
    else {
      UiEventDispatcher.createNode(node())
    }
    ae.consume()
  })

  val nodeNameField = new TextField() {
    text <==> node().nameProperty

    node onChange { (_, oldNode, newNode) =>
      text.unbind(oldNode.nameProperty)
      text <==> newNode.nameProperty
    }
  }
  val startNodeCheckbox = new CheckBox("Start node") {
    allowIndeterminate = false

    selected <==> node().isStartNodeProperty
    disable <== selected

    node onChange { (_, oldNode, newNode) =>
      selected.unbind(oldNode.isStartNodeProperty)
      selected <==> newNode.isStartNodeProperty
    }
  }

  val rulesetsList = new ListView[UiRuleset] {
    cellFactory = { _ =>
      new ListCell[UiRuleset] {
        item onChange { (_, _, nullableNewRuleset) =>
          Option(nullableNewRuleset) match {
            case Some(ruleset) =>
              val nameField = new TextField {
                text <==> ruleset.nameProperty

                focused onChange { (_, _, nullableIsFocused) =>
                  Option(nullableIsFocused) foreach { isFocused =>
                    selectionModel().select(ruleset)
                  }
                }

                HBox.setHgrow(this, Priority.Always)
              }
              val removeButton = new Button("−") {
                onAction = { _ =>
                  node().contentProperty().rulesetsProperty() -= ruleset
                  nodeContentText.setStyle(ruleset.indexes.startIndex.index.toInt,
                                           ruleset.indexes.endIndex.index.toInt,
                                           new LinkStyleInfo())
                }
              }

              graphic = new HBox(10, removeButton, nameField)
            case None => graphic = null
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

    nodeContentText.selection onChange { (_, _, selectedRange) =>
      val foldedStyleSpans = nodeContentText.styleSpansAt(selectedRange).foldLeft(List.empty[StyleSpan[LinkStyleInfo]]) {
        case (resList @ headSpan :: tail, currSpan) =>
          if (headSpan == currSpan) resList else currSpan :: resList
        case (Nil, currSpan) => currSpan :: Nil
      }

      if (foldedStyleSpans.size == 1) {
        val selectedSpan = foldedStyleSpans.head

        selectedSpan.getStyle.ruleset match {
          case Some(r) =>
            selectionModel().select(r)
            scrollTo(r)
          case None =>
            selectionModel().clearSelection()
        }
      }
      else selectionModel().clearSelection()
    }

    items <== monadicNode flatMap[UiNodeContent] (_.contentProperty) flatMap[ObservableList[UiRuleset]] (_.rulesetsProperty)
  }

  lazy val nodeContentText = new NodeContentTextArea {
    setWrapText(true)
    replaceText(node().content.text)
    node().content.rulesetsProperty() foreach { ruleset =>
      setStyle(ruleset.indexes.startIndex.index.toInt,
               ruleset.indexes.endIndex.index.toInt,
               new LinkStyleInfo(Some(ruleset)))
    }

    getUndoManager.forgetHistory() // Ensure that the initialisation of the text done above is not undoable

    var indexUpdateSub = beingUpdatedProperty() onChange { (_, _, beingUpdated) =>
      if (!beingUpdated) updateNodeContentRulesetsIndexes()
    }

    node onChange { (_, _, newNode) =>
      indexUpdateSub.cancel()
      replaceText(newNode.content.text)
      if (newNode.content.text.length > 0) {
        setStyle(0, text().length, new LinkStyleInfo()) // Clear text styling before applying text styling from rulesets
      }
      newNode.content.rulesetsProperty() foreach { ruleset =>
        setStyle(ruleset.indexes.startIndex.index.toInt,
                 ruleset.indexes.endIndex.index.toInt,
                 new LinkStyleInfo(Some(ruleset)))
      }

      getUndoManager.forgetHistory() // Ensure that the initialisation of the text done above is not undoable

      indexUpdateSub = beingUpdatedProperty() onChange { (_, _, beingUpdated) =>
        if (!beingUpdated) updateNodeContentRulesetsIndexes()
      }
    }

    styleCodec = new Codec[LinkStyleInfo] {
      override def getName: String = "link-style-info"

      override def encode(os: DataOutputStream, t: LinkStyleInfo): Unit = {
        import org.narrativeandplay.hypedyn.core.serialisation.serialisers.RulesetSerialiser
        import org.narrativeandplay.hypedyn.core.story.InterfaceToImplementationConversions.rulesetLike2Ruleset
        import org.narrativeandplay.hypedyn.core.serialisation.serialisers.JsonSerialiser

        t.ruleset match {
          case Some(r) =>
            os.writeBoolean(true)
            os.writeUTF(JsonSerialiser.serialise(RulesetSerialiser.serialise(r)))
          case None =>
            os.writeBoolean(false)
        }
      }

      override def decode(is: DataInputStream): LinkStyleInfo = {
        import org.narrativeandplay.hypedyn.core.serialisation.serialisers.RulesetSerialiser
        import org.narrativeandplay.hypedyn.core.serialisation.serialisers.JsonSerialiser

        is.readBoolean() match {
          case true =>
            val ruleset = RulesetSerialiser.deserialise(JsonSerialiser.deserialise(is.readUTF()))
            val copiedRules = ruleset.rules map (_.copy(id = RuleId(-1)))
            val copiedRuleset = ruleset.copy(id = firstUnusedRulesetId, rules = copiedRules)
            firstUnusedRulesetId = firstUnusedRulesetId.dec

            new LinkStyleInfo(Some(copiedRuleset))
          case false =>
            new LinkStyleInfo()
        }
      }
    }
  }

  lazy val textRulesPane: RulesPane = new RulesPane("Fragment rules",
                                                    conditionDefinitions,
                                                    actionDefinitions filter (_.actionLocationTypes contains NodeContentAction),
                                                    ObservableBuffer.empty,
                                                    story) {
    disableAddRule <== rulesetsList.selectionModel().selectedItem.isNull
  }
  val nodeRulesPane = new RulesPane("Node rules",
                                    conditionDefinitions,
                                    actionDefinitions filter (_.actionLocationTypes contains NodeAction),
                                    node().rulesProperty(),
                                    story) {
    rules <== monadicNode flatMap[ObservableList[UiRule]] (_.rulesProperty)
  }

  val rulesetsListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add fragment") {
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
                                         "new fragment",
                                         RulesetIndexes(TextIndex(start), TextIndex(end)),
                                         Nil)
          firstUnusedRulesetId = firstUnusedRulesetId.dec
          node().contentProperty().rulesetsProperty() += newRuleset
          nodeContentText.setStyle(start,
                                   end,
                                   new LinkStyleInfo(Some(newRuleset)))
          rulesetsList.selectionModel().select(newRuleset)
        }
      }
    }
    children += rulesetsList

    VBox.setVgrow(rulesetsList, Priority.Always)
  }
  val contentTextAndRulesetsListPane = new CollapsibleSplitPane {
    orientation = Orientation.Horizontal
    add(rulesetsListVBox)
    add(new VBox {
      children += nodeContentText

      VBox.setVgrow(nodeContentText, Priority.Always)
    })

    dividerPositions = 0.3
  }

  dialogPane().scene().stylesheets += getClass.getResource("/org/narrativeandplay/hypedyn/tab-pane-fix.css").toExternalForm
  val textAndNodeRulesPane = new TabPane {
    tabClosingPolicy = TabPane.TabClosingPolicy.Unavailable

    tabs += new Tab {
      text = "Fragment rules"
      content = textRulesPane
    }
    tabs += new Tab {
      text = "Node rules"
      content = nodeRulesPane
    }
  }

  val mainContentPane = new CollapsibleSplitPane {
    orientation = Orientation.Vertical
    VBox.setVgrow(this, Priority.Always)

    add(contentTextAndRulesetsListPane)

    add(textAndNodeRulesPane)

    dividerPositions = 0.75
  }

  val contentPane = new BorderPane() {
    top = new VBox() {
      children += new HBox(10) {
        padding = Insets(5, 0, 5, 0)
        alignment = Pos.CenterLeft

        children += new Label("Name:")
        children += nodeNameField
        children += startNodeCheckbox

        HBox.setHgrow(nodeNameField, Priority.Always)
      }
    }

    center = mainContentPane

    bottom = new ToolBar {
      style = "-fx-background-color: transparent;"

      // Blank and invisible button to push the next button into a nice position
      items += new Button("") {
        minWidth = 35
        visible = false
      }
      items += new Button {
        text = "Hide rules"

        onAction = { _ =>
          mainContentPane isShown textAndNodeRulesPane match {
            case true => mainContentPane.hide(textAndNodeRulesPane)
              text = "Show rules"
            case false => mainContentPane.show(textAndNodeRulesPane)
              text = "Hide rules"
          }
        }
      }
    }

    left = new ToolBar {
      style = "-fx-background-color: transparent;"
      orientation = Orientation.Vertical

      items += new SidebarButton("Hide fragments") {
        onAction = { _ =>
          contentTextAndRulesetsListPane isShown rulesetsListVBox match {
            case true => contentTextAndRulesetsListPane.hide(rulesetsListVBox)
              buttonText = "Show fragments"
            case false => contentTextAndRulesetsListPane.show(rulesetsListVBox)
              buttonText = "Hide fragments"
          }
        }
      }
    }
  }

  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      node().contentProperty().textProperty() = nodeContentText.getText
      updateNodeContentRulesetsIndexes()
      node()
    case _ => null
  }

  onShown = { _ =>
    nodeNameField.requestFocus()
  }

  /**
   * Updates the indexes for where a text rule belongs in the text, and removes rulesets for which there is no
   * corresponding text (i.e., when text containing a text rule has been deleted).
   */
  def updateNodeContentRulesetsIndexes(): Unit = {
    import org.narrativeandplay.hypedyn.api.story.NodalContent._
    val spans = nodeContentText.styleSpans.scanLeft((0, 0, Option.empty[UiRuleset])) {
      case ((_, end, rulesetOption), styleSpan) =>
        (end, end + styleSpan.getLength, styleSpan.getStyle.ruleset)
    }
    val rulesetsExistingInText = (spans.tail foldLeft List(spans.head)) {
      case (resList @ (headStart, _, headSpan) :: tail, (start, end, span)) =>
        if (headSpan == span) (headStart, end, span) :: tail else (start, end, span) :: resList
      case (Nil, input) => input :: Nil
    } collect { case (start, end, Some(ruleset)) =>
      ruleset.indexesProperty() = RulesetIndexes(TextIndex(start), TextIndex(end))
      ruleset
    }

    val rulesetsToRemove = node().contentProperty().rulesetsProperty().toList filterNot rulesetsExistingInText.toSet
    rulesetsToRemove foreach { r => node().contentProperty().rulesetsProperty() -= r }

    val rulesetsToAdd = rulesetsExistingInText filterNot node().contentProperty().rulesetsProperty().toSet
    rulesetsToAdd foreach { r => node().contentProperty().rulesetsProperty() += r }

    /**
     * Special case for dealing with issue #8
     *
     * If the entire text of the text area is styled in a non-initial style, and you remove all the text,
     * any new text will be styled using the non-initial style. The style is sticky, somehow, and there
     * exists a style span of the non-initial style from position 0 to position 0.
     *
     * This is the only allowed special case in handing rule addition/removal from the text. If any other
     * weird thing pops up, find a general solution, or file an issue against RichTextFX.
     */
    node().contentProperty().rulesetsProperty() find { r =>
      r.indexes.startIndex == TextIndex(0) && r.indexes.endIndex == TextIndex(0)
    } foreach { r =>
      node().contentProperty().rulesetsProperty() -= r
      nodeContentText.useInitialStyleForInsertion = true
      nodeContentText.insertText(0, " ")
      nodeContentText.useInitialStyleForInsertion = false
      nodeContentText.replaceText("")
    }
  }

  def updateNodeData(updatedNode: UiNode): Unit = {
    node() = updatedNode
    updateFunc = UiEventDispatcher.updateNode(updatedNode.copy)
  }

  def id = node().id

  /**
   * Shows a blocking node editor dialog
   *
   * @return An option containing the edited face, or None if the dialog was not closed with the OK button
   */
  def showAndWait(): Option[Nodal] = {
    initModality(Modality.ApplicationModal)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}

object NodeEditor {
  private[this] var firstUnusedNodeId = NodeId(-1)
  private def newNode = {
    val newNode = new UiNode(firstUnusedNodeId, "New Node", new UiNodeContent("", Nil), false, Nil)
    firstUnusedNodeId = firstUnusedNodeId.dec
    newNode
  }

  /**
   * Style class for styling node text, as well as attaching user data (the ruleset for a span of text)
   *
   * @param ruleset An option containing the ruleset attached to this span of text, or None if no ruleset is
   *                attached
   */
  class LinkStyleInfo(val ruleset: Option[UiRuleset] = None) {
    private val linkStyle = "-fx-font-weight: bold; -fx-underline: true;"

    def css = if (ruleset.isDefined) linkStyle else ""

    override def toString = s"hasRule: ${ruleset.isDefined}"
  }
  private[this] def applyParagraphStyle = new BiConsumer[TextFlow, util.Collection[String]] {
    override def accept(paragraph: TextFlow, styleClasses: util.Collection[String]) =
      paragraph.getStyleClass.addAll(styleClasses)
  }
  private[this] def applyStyle = new BiConsumer[Text, LinkStyleInfo] {
    override def accept(text: Text, style: LinkStyleInfo) = text.setStyle(style.css)
  }

  /**
   * An extended rich text area to provide some convenience methods
   */
  class NodeContentTextArea extends StyledTextArea[util.Collection[String], LinkStyleInfo](
    util.Collections.emptyList(),
    applyParagraphStyle,
    new LinkStyleInfo(),
    applyStyle
  ) {
    addEventFilter(KeyEvent.KeyTyped, { keyEvent: JfxKeyEvent =>
      if (keyEvent.shiftDown && keyEvent.character == " ") {
        useInitialStyleForInsertion = true

        if (styleAt(caretPosition()).ruleset.isDefined &&
            (styleAt(caretPosition() + 1).ruleset != styleAt(caretPosition()).ruleset
              || caretPosition() == 0 // Because the style 'sticks' to the first position if the text after it is styled
              || caretPosition() == text().length)) // Because the style at the next position after the end isn't well
                                                    // defined
          insertText(caretPosition(), " ")

        useInitialStyleForInsertion = false

        // Shift-space also inserts a space on its own, so we consume the event to prevent double space insertion
        keyEvent.consume()
      }
    })

    onMouseClicked = { me =>
      me.clickCount match {
        case 1 =>
          val hasNoSelectedText = getSelectedText == ""
          val styleAtCaret = styleAt(caretPosition())
          val selectedPosHasRule = styleAtCaret.ruleset.isDefined

          if (hasNoSelectedText && selectedPosHasRule) {
            val rulesetIndexes = styleAtCaret.ruleset.get.indexes
            val ruleRange = new IndexRange(rulesetIndexes.startIndex.index.toInt,
                                           rulesetIndexes.endIndex.index.toInt)

            selectRange(ruleRange.start, ruleRange.end)
          }
        case _ =>
      }
    }

    /**
     * Returns all the style spans in the text
     */
    def styleSpans = {
      import scala.collection.JavaConverters._

      // `getStyleSpans` returns a `StyleSpans[S]` which is a Java
      // `Iterable[S]`, so we convert that into a Scala `Iterable[S]`
      // and finally convert that into a `List[S]`
      getStyleSpans(0, getText.length).asScala.toList
    }

    /**
     * Returns all the style spans within the given range of text
     *
     * @param indexRange The range of text for which to get the style spans
     */
    def styleSpansAt(indexRange: IndexRange) = {
      import scala.collection.JavaConverters._

      // `getStyleSpans` returns a `StyleSpans[S]` which is a Java
      // `Iterable[S]`, so we convert that into a Scala `Iterable[S]`
      // and finally convert that into a `List[S]`
      getStyleSpans(indexRange).asScala.toList
    }

    def styleAt(position: Int) = getStyleAtPosition(position)

    def useInitialStyleForInsertion = useInitialStyleForInsertionProperty()
    def useInitialStyleForInsertion_=(value: Boolean) = setUseInitialStyleForInsertion(value)

    def selection = selectionProperty()

    def caretPosition = caretPositionProperty()

    def text = textProperty()



    def styleCodec = getStyleCodecs.get()._2
    def styleCodec_=(codec: Codec[LinkStyleInfo]) = setStyleCodecs(Codec.collectionCodec(Codec.STRING_CODEC), StyledText.codec(codec))

    def onMouseClicked = { me: MouseEvent => getOnMouseClicked.handle(me) }
    def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
      setOnMouseClicked(new EventHandler[U] {
        override def handle(event: U): Unit = lambda(event)
      })
    }
  }
}
