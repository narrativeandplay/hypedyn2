package org.narrativeandplay.hypedyn.uicomponents

import java.util.function.{Function => JFunction}
import javafx.beans.property
import javafx.collections.ObservableList
import javafx.scene.control.{SpinnerValueFactory => JfxSpinnerValueFactory, ListCell => JfxListCell}

import scala.util.Try

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.{ObservableBuffer, ObservableMap}
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, StackPane, Region, HBox}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.story.rules.ParameterValues._
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._


object RuleCellComponents {

  /**
   * Common interface for parameter component UIs
   */
  sealed trait RuleCellParameterComponent {
    /**
     * Returns the property containing the story for the component
     */
    def story: ObjectProperty[UiStory]

    /**
     * Returns the name of the parameter this component represents
     */
    def paramName: ParamName

    /**
     * Returns the parameter map this component manipulates
     */
    def paramMap: ObservableMap[ParamName, ParamValue]

    /**
     * Returns an option containing the value of this parameter, or None if it's value has not been set
     */
    def `val`: Option[ParamValue]

    /**
     * Sets the value of the parameter
     *
     * @param string The value to set it to
     */
    def val_=(string: ParamValue): Unit
  }

  object RuleCellParameterComponent {
    case class InvalidParamValueException(msg: String) extends Exception(msg)
  }

  /**
   * Cell for manipulating a condition
   *
   * @param condition The condition to manipulate
   * @param conditionDefinitions The list of condition definitions
   * @param parentStory The property containing story the parent belongs to
   * @param parentRule The rule the parent belongs to
   */
  class ConditionCell(val condition: UiCondition, 
                      val conditionDefinitions: List[ConditionDefinition],
                      val parentStory: ObjectProperty[UiStory],
                      val parentCell: RuleCell,
                      parentRule: UiRule) extends TreeItem[String]("") {
    private def parentTreeItem = parent()
    private val self = this

    val condParamsChildren = ObservableBuffer.empty[Region with RuleCellParameterComponent]

    val condTypeCombo = new ComboBox[ConditionDefinition] {
      items = ObservableBuffer(conditionDefinitions)

      converter = new StringConverter[ConditionDefinition] {
        override def fromString(string: String): ConditionDefinition = (conditionDefinitions find (_.description == string)).get

        override def toString(t: ConditionDefinition): String = t.description
      }

      cellFactory = { _ =>
        new JfxListCell[ConditionDefinition] {
          override def updateItem(item: ConditionDefinition, empty: Boolean): Unit = {
            super.updateItem(item, empty)

            if (!empty && item != null) {
              setText(item.description)
            }
          }
        }
      }

      onAction = { _ =>
        condition.paramsProperty().clear()
        condition.conditionTypeProperty() = selectionModel().getSelectedItem.conditionType

        condParams.children.clear()
        condParamsChildren.clear()

        generateParameterInputComponents(this)
      }

      value = (conditionDefinitions find (_.conditionType == condition.conditionType)).get
      generateParameterInputComponents(this)
      condParamsChildren foreach { component =>
        condition.params get component.paramName foreach { v => component.`val` = v }
      }
    }

    lazy val condParams = new HBox {
      HBox.setHgrow(this, Priority.Always)
    }

    lazy val removeButton = new StackPane {
      padding = Insets(0, 10, 0, 0)
      children += new Button("−") {
        onAction = { _ =>
          parentTreeItem.getChildren.remove(self)
          parentRule.conditionsProperty() -= condition
        }
      }
    }

    lazy val moveUpButton = new Button("↑") {
      disable = parentRule.conditions.head == condition

      onAction = { _ =>
        val currentIndex = parentRule.conditions indexOf condition
        val newIndex = currentIndex - 1

        val itemToMoveDown = parentRule.conditions(newIndex)

        parentRule.conditionsProperty().set(newIndex, condition)
        parentRule.conditionsProperty().set(currentIndex, itemToMoveDown)

        parentCell.rearrangeConditions()
      }
    }
    lazy val moveDownButton = new Button("↓") {
      disable = parentRule.conditions.last == condition

      onAction = { _ =>
        val currentIndex = parentRule.conditions indexOf condition
        val newIndex = currentIndex + 1

        val itemToMoveUp = parentRule.conditions(newIndex)

        parentRule.conditionsProperty().set(newIndex, condition)
        parentRule.conditionsProperty().set(currentIndex, itemToMoveUp)

        parentCell.rearrangeConditions()
      }
    }
    lazy val rearrangeButtons = new HBox(10, moveUpButton, moveDownButton) {
      padding = Insets(0, 10, 0, 0)
    }

    parentRule.conditionsProperty() onChange { (buffer, changes) =>
      if (buffer.nonEmpty) {
        moveUpButton.disable = buffer.head == condition
        moveDownButton.disable = buffer.last == condition
      }
    }

    /**
     * Generate the UI components for the selected condition
     *
     * @param comboBox The combo box for selecting the condition type
     */
    private def generateParameterInputComponents(comboBox: ComboBox[ConditionDefinition]) =
      comboBox.selectionModel().getSelectedItem.parameters foreach { p =>
        val newComponent = createParameterInput(p, condition.paramsProperty(), parentStory)
        condParams.children += newComponent
        condParamsChildren += newComponent
      }

    graphic = new HBox(rearrangeButtons, removeButton, condTypeCombo, condParams)
    
  }

  /**
   * Cell for manipulating an action
   *
   * @param action The action to manipulate
   * @param actionDefinitions The list of action definitions
   * @param parentStory The story the action belongs to
   * @param parentRule The rule the action belongs to
   */
  class ActionCell(val action: UiAction,
                   val actionDefinitions: List[ActionDefinition],
                   val parentStory: ObjectProperty[UiStory],
                   val parentCell: RuleCell,
                   parentRule: UiRule) extends TreeItem[String]("") {
    private def parentTreeItem = parent()
    private val self = this

    val actionParamsChildren = ObservableBuffer.empty[Region with RuleCellParameterComponent]

    val actionTypeCombo = new ComboBox[ActionDefinition]() {
      items = ObservableBuffer(actionDefinitions)

      cellFactory = { _ =>
        new JfxListCell[ActionDefinition] {
          override def updateItem(item: ActionDefinition, empty: Boolean): Unit = {
            super.updateItem(item, empty)

            if (!empty && item != null) {
              setText(item.description)
            }
          }
        }
      }

      converter = new StringConverter[ActionDefinition] {
        override def fromString(string: String): ActionDefinition = (actionDefinitions find (_.description == string)).get

        override def toString(t: ActionDefinition): String = t.description
      }

      onAction = { _ =>
        action.paramsProperty().clear()
        action.actionTypeProperty() = selectionModel().getSelectedItem.actionType

        actionParams.children.clear()
        actionParamsChildren.clear()

        generateParameterInputComponents(this)
      }

      value = (actionDefinitions find (_.actionType == action.actionType)).get
      generateParameterInputComponents(this)
      actionParamsChildren foreach { component =>
        action.params get component.paramName foreach { v => component.`val` = v }
      }
    }

    lazy val actionParams = new HBox {
      HBox.setHgrow(this, Priority.Always)
    }

    lazy val removeButton = new StackPane {
      padding = Insets(0, 10, 0, 0)
      children += new Button("−") {
        minWidth = 25
        onAction = { _ =>
          parentTreeItem.getChildren.remove(self)
          parentRule.actionsProperty() -= action
        }
      }
    }

    lazy val moveUpButton = new Button("↑") {
      disable = parentRule.actions.head == action

      onAction = { _ =>
        val currentIndex = parentRule.actions indexOf action
        val newIndex = currentIndex - 1

        val itemToMoveDown = parentRule.actions(newIndex)

        parentRule.actionsProperty().set(newIndex, action)
        parentRule.actionsProperty().set(currentIndex, itemToMoveDown)

        parentCell.rearrangeActions()
      }
    }
    lazy val moveDownButton = new Button("↓") {
      disable = parentRule.actions.last == action

      onAction = { _ =>
        val currentIndex = parentRule.actions indexOf action
        val newIndex = currentIndex + 1

        val itemToMoveUp = parentRule.actions(newIndex)

        parentRule.actionsProperty().set(newIndex, action)
        parentRule.actionsProperty().set(currentIndex, itemToMoveUp)

        parentCell.rearrangeActions()
      }
    }
    lazy val rearrangeButtons = new HBox(10, moveUpButton, moveDownButton) {
      padding = Insets(0, 10, 0, 0)
    }

    parentRule.actionsProperty() onChange { (buffer, changes) =>
      if (buffer.nonEmpty) {
        moveUpButton.disable = buffer.head == action
        moveDownButton.disable = buffer.last == action
      }
    }

    /**
     * Generate the UI components for the selected condition
     *
     * @param comboBox The combo box for selecting the action type
     */
    private def generateParameterInputComponents(comboBox: ComboBox[ActionDefinition]) =
      comboBox.selectionModel().getSelectedItem.parameters foreach { p =>
        val newComponent = createParameterInput(p, action.paramsProperty(), parentStory)
        actionParams.children += newComponent
        actionParamsChildren += newComponent
      }

    graphic = new HBox(rearrangeButtons, removeButton, actionTypeCombo, actionParams)
  }

  /**
   * Creates a UI component for a given rule parameter
   *
   * @param ruleParameter The rule parameter to generate UI components for
   * @param parameterMap The parameter map which the component has access to
   * @param parentStory The story the parameter map belongs to
   * @return The created component
   */
  private def createParameterInput(ruleParameter: RuleParameter,
                                   parameterMap: ObservableMap[ParamName, ParamValue],
                                   parentStory: ObjectProperty[UiStory]) = ruleParameter.possibleValues match {
    case Nodes => new NodeComboBox(parameterMap, ParamName(ruleParameter.name), parentStory)
    case Links => new LinkComboBox(parameterMap, ParamName(ruleParameter.name), parentStory)
    case IntegerFacts => new IntegerFactsComboBox(parameterMap, ParamName(ruleParameter.name), parentStory)
    case BooleanFacts => new BooleanFactsComboBox(parameterMap, ParamName(ruleParameter.name), parentStory)
    case StringFacts => new StringFactsComboBox(parameterMap, ParamName(ruleParameter.name), parentStory)
    case UserInputString => new StringInput(parameterMap, ParamName(ruleParameter.name), parentStory)
    case UserInputInteger => new IntegerInput(parameterMap, ParamName(ruleParameter.name), parentStory)
    case ListOfValues(vals @ _*) => new ValuesListComboBox(parameterMap, ParamName(ruleParameter.name), parentStory, vals)
    case Union(params) => new UnionComboBoxPane(parameterMap, ParamName(ruleParameter.name), parentStory, params)
    case Product(params) => new ProductPane(parameterMap, ParamName(ruleParameter.name), parentStory, params)
  }

  /**
   * Combo box for selecting a node
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class NodeComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                     val paramName: ParamName,
                     val story: ObjectProperty[UiStory]) extends ComboBox[UiNode] with RuleCellParameterComponent{
    cellFactory = { _ =>
      new JfxListCell[UiNode] {
        override def updateItem(item: UiNode, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[UiNode] {
      override def fromString(string: String): UiNode = (story().nodes find (_.name == string)).get

      override def toString(t: UiNode): String = t.name
    }

    onAction = { _ =>
      Option(value()) foreach { v => paramMap += paramName -> ParamValue.Node(v.id) }
    }

    items <== EasyBind monadic story flatMap[ObservableList[UiNode]] (_.nodesProperty)

    items onChange {
      `val` foreach {
        case ParamValue.Node(id) =>
          story().nodes find (_.id == id) foreach { n => value = n }
        case pv =>
          throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.Node, got: $pv")
      }
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val idToSet = paramValue match {
        case ParamValue.Node(n) => n
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.Node, got: $pv")
      }
      story().nodes find (_.id == idToSet) foreach { n => value = n }
    }
  }

  /**
   * Combo box for selecting a link
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class LinkComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                     val paramName: ParamName,
                     val story: ObjectProperty[UiStory]) extends ComboBox[UiNodeContent.UiRuleset] with RuleCellParameterComponent {
    cellFactory = { _ =>
      new JfxListCell[UiNodeContent.UiRuleset] {
        override def updateItem(item: UiNodeContent.UiRuleset, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[UiNodeContent.UiRuleset] {
      override def fromString(string: String): UiNodeContent.UiRuleset = (story().canActivate find (_.name == string)).get

      override def toString(t: UiNodeContent.UiRuleset): String = t.name
    }

    onAction = { _ =>
      Option(value()) foreach { v => paramMap += paramName -> ParamValue.Link(v.id) }
    }

    items <== EasyBind monadic story map[ObservableList[UiNodeContent.UiRuleset]] (_.canActivate)

    items onChange {
      `val` foreach {
        case ParamValue.Link(id) =>
          story().canActivate find (_.id == id) foreach { r => value = r }
        case pv =>
          throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.Link, got: $pv")
      }
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val idToSet = paramValue match {
        case ParamValue.Link(l) => l
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.Link, got: $pv")
      }
      story().canActivate find (_.id == idToSet) foreach { r => value = r }
    }
  }

  /**
   * Combo box for selecting an integer fact
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class IntegerFactsComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                             val paramName: ParamName,
                             val story: ObjectProperty[UiStory]) extends ComboBox[IntegerFact] with RuleCellParameterComponent {
    cellFactory = { _ =>
      new JfxListCell[IntegerFact] {
        override def updateItem(item: IntegerFact, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[IntegerFact] {
      override def fromString(string: String): IntegerFact = (intFacts find (_.name == string)).get

      override def toString(t: IntegerFact): String = t.name
    }

    onAction = { _ =>
      Option(selectionModel().selectedItem()) foreach { f => paramMap += paramName -> ParamValue.IntegerFact(f.id) }
    }

    items <== EasyBind monadic story flatMap[ObservableList[Fact]] (_.factsProperty) map[ObservableList[IntegerFact]] { facts =>
      facts collect { case f: IntegerFact => f }
    }

    items onChange {
      `val` foreach {
        case ParamValue.IntegerFact(id) =>
          intFacts find (_.id == id) foreach { f => value = f }
        case pv =>
          throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.IntegerFact, got: $pv")
      }
    }

    def intFacts = story().factsProperty() collect { case f: IntegerFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val idToSet = paramValue match {
        case ParamValue.IntegerFact(f) => f
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.IntegerFact, got: $pv")
      }
      intFacts find (_.id == idToSet) foreach { f => value = f }
    }
  }

  /**
   * Combo box for selecting a boolean fact
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class BooleanFactsComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                             val paramName: ParamName,
                             val story: ObjectProperty[UiStory]) extends ComboBox[BooleanFact] with RuleCellParameterComponent {
    cellFactory = { _ =>
      new JfxListCell[BooleanFact] {
        override def updateItem(item: BooleanFact, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[BooleanFact] {
      override def fromString(string: String): BooleanFact = (boolFacts find (_.name == string)).get

      override def toString(t: BooleanFact): String = t.name
    }

    onAction = { _ =>
      Option(selectionModel().selectedItem()) foreach { f => paramMap += paramName -> ParamValue.BooleanFact(f.id) }
    }

    items <== EasyBind monadic story flatMap[ObservableList[Fact]] (_.factsProperty) map[ObservableList[BooleanFact]] { facts =>
      facts collect { case f: BooleanFact => f }
    }

    items onChange {
      `val` foreach {
        case ParamValue.BooleanFact(id) =>
          boolFacts find (_.id == id) foreach { f => value = f }
        case pv =>
          throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.BooleanFact, got: $pv")
      }
    }

    def boolFacts = story().factsProperty() collect { case f: BooleanFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val idToSet = paramValue match {
        case ParamValue.BooleanFact(f) => f
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.BooleanFact, got: $pv")
      }
      boolFacts find (_.id == idToSet) foreach { f => value =  f }
    }
  }

  /**
   * Combo box for selecting a string fact
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class StringFactsComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                            val paramName: ParamName,
                            val story: ObjectProperty[UiStory]) extends ComboBox[StringFact] with RuleCellParameterComponent {
    cellFactory = { _ =>
      new JfxListCell[StringFact] {
        override def updateItem(item: StringFact, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[StringFact] {
      override def fromString(string: String): StringFact = (stringFacts find (_.name == string)).get

      override def toString(t: StringFact): String = t.name
    }

    onAction = { _ =>
      Option(selectionModel().selectedItem()) foreach { f => paramMap += paramName -> ParamValue.StringFact(f.id) }
    }

    items <== EasyBind monadic story flatMap[ObservableList[Fact]] (_.factsProperty) map[ObservableList[StringFact]] { facts =>
      facts collect { case f: StringFact => f }
    }

    items onChange {
      `val` foreach {
        case ParamValue.StringFact(id) =>
          stringFacts find (_.id == id) foreach { f => value = f }
        case pv =>
          throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.StringFact, got: $pv")
      }
    }

    def stringFacts = story().factsProperty() collect { case f: StringFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val idToSet = paramValue match {
        case ParamValue.StringFact(f) => f
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.StringFact, got: $pv")
      }
      stringFacts find (_.id == idToSet) foreach { f => value = f }
    }
  }

  /**
   * Text area for a string input
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class StringInput(val paramMap: ObservableMap[ParamName, ParamValue],
                    val paramName: ParamName,
                    val story: ObjectProperty[UiStory]) extends TextArea with RuleCellParameterComponent {
    `val` match {
      case None => paramMap += paramName -> ParamValue.StringInput(text())
      case _ =>
    }

    text onChange { (_, _ , newValue) =>
      Option(newValue) foreach (paramMap += paramName -> ParamValue.StringInput(_))
    }

    prefWidth = 150
    prefHeight = 75

    wrapText = true

    HBox.setHgrow(this, Priority.Always)

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val textToSet = paramValue match {
        case ParamValue.StringInput(s) => s
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.StringInput, got: $pv")
      }
      text = textToSet
    }
  }

  /**
   * Spinner for an integer input
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   */
  class IntegerInput(val paramMap: ObservableMap[ParamName, ParamValue],
                     val paramName: ParamName,
                     val story: ObjectProperty[UiStory]) extends Spinner[BigInt] with RuleCellParameterComponent {
    valueFactory = new JfxSpinnerValueFactory[BigInt] {
      setValue(0)

      setConverter(new StringConverter[BigInt] {
        override def fromString(string: String): BigInt = Try(BigInt(string)) getOrElse getValue

        override def toString(t: BigInt): String = t.toString()
      })

      override def increment(steps: Int): Unit = setValue(getValue + steps)

      override def decrement(steps: Int): Unit = setValue(getValue - steps)
    }

    paramMap += paramName -> ParamValue.IntegerInput(value())

    value onChange { (_, _, newValue) =>
      Option(newValue) foreach { v => paramMap += paramName -> ParamValue.IntegerInput(v) }
    }

    editable = true
    editor().text onChange { (_, _, _) =>
      valueFactory().value = valueFactory().converter().fromString(editor().text())
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val intToSet = paramValue match {
        case ParamValue.IntegerInput(i) => i
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.IntegerInput, got: $pv")
      }
      valueFactory().setValue(intToSet)
    }
  }

  /**
   * Combo box for selecting a value from a list of strings
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   * @param values The list of strings to select a value from
   */
  class ValuesListComboBox(val paramMap: ObservableMap[ParamName, ParamValue],
                           val paramName: ParamName,
                           val story: ObjectProperty[UiStory],
                           values: Seq[String]) extends ComboBox[String] with RuleCellParameterComponent {
    items = ObservableBuffer(values)

    onAction = { _ =>
      paramMap += paramName -> ParamValue.SelectedListValue(selectionModel().getSelectedItem)
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val valToSet = paramValue match {
        case ParamValue.SelectedListValue(v) => v
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.SelectedListValue, got: $pv")
      }
      value = valToSet
    }
  }

  /**
   * Combo box for selecting a rule parameter type from a list of rule parameter types
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   * @param params The map of parameter types to select from; the keys are the display names
   */
  class UnionComboBoxPane(val paramMap: ObservableMap[ParamName, ParamValue],
                          val paramName: ParamName,
                          val story: ObjectProperty[UiStory],
                          params: Map[String, RuleParameter]) extends HBox with RuleCellParameterComponent {
    val unionParamsChildren = ObservableBuffer.empty[Region with RuleCellParameterComponent]

    val unionValue = new ComboBox[(String, RuleParameter)]() {
      cellFactory = { _ =>
        new JfxListCell[(String, RuleParameter)] {
          override def updateItem(item: (String, RuleParameter), empty: Boolean): Unit = {
            super.updateItem(item, empty)

            if (!empty && item != null) {
              setText(item._1)
            }
          }
        }
      }

      converter = new StringConverter[(String, RuleParameter)] {
        override def fromString(string: String): (String, RuleParameter) = string -> params(string)

        override def toString(t: (String, RuleParameter)): String = t._1
      }

      items = ObservableBuffer(params.toSeq)

      onAction = { _ =>
        val selected = selectionModel().getSelectedItem._2

        pane.children.clear()

        paramMap += paramName -> ParamValue.UnionValueSelected(selected.name)

        generateParameterInputComponents(this)
      }
    }

    lazy val pane = new HBox {
      HBox.setHgrow(this, Priority.Always)
    }

    HBox.setHgrow(this, Priority.Always)

    children.addAll(unionValue, pane)

    private def generateParameterInputComponents(comboBox: ComboBox[(String, RuleParameter)]) = {
      val newComponent = createParameterInput(comboBox.value()._2, paramMap, story)
      pane.children += newComponent
      unionParamsChildren += newComponent
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = {
      val selectedParameterName = paramValue match {
        case ParamValue.UnionValueSelected(s) => s
        case pv => throw RuleCellParameterComponent.InvalidParamValueException(s"Expected type ParamValue.UnionValueSelected, got: $pv")
      }
      params find { case (_, param) => param.name == selectedParameterName } foreach { p => unionValue.value = p }

      unionParamsChildren.clear()
      pane.children.clear()

      generateParameterInputComponents(unionValue)
      unionParamsChildren foreach { component =>
        paramMap get component.paramName foreach { v => component.`val` = v }
      }
    }
  }

  /**
   * Pane for containing a group of rule parameters
   *
   * @param paramMap The paramter map this component manipulates
   * @param paramName The name of the parameter this component manipulates
   * @param story The property containing the story for the component
   * @param params The list of parameters in the group
   */
  class ProductPane(val paramMap: ObservableMap[ParamName, ParamValue],
                    val paramName: ParamName,
                    val story: ObjectProperty[UiStory],
                    params: List[RuleParameter]) extends HBox with RuleCellParameterComponent {
    paramMap += paramName -> ParamValue.ProductValue(params map (_.name))

    params foreach { param =>
      val newComponent = createParameterInput(param, paramMap, story)
      children +=  newComponent
      paramMap get newComponent.paramName foreach { v => newComponent.`val` = v }
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = ()
  }
}
