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
import scalafx.scene.layout.{StackPane, Region, HBox}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.story.rules.ParameterValues._
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.utils.ScalaJavaImplicits._


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

    lazy val condParams = new HBox()

    lazy val removeButton = new StackPane {
      padding = Insets(0, 0, 0, 10)
      children += new Button("-") {
        onAction = { _ =>
          parentTreeItem.getChildren.remove(self)
          parentRule.conditionsProperty() -= condition
        }
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

    graphic = new HBox(condTypeCombo, condParams, removeButton)
    
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

    lazy val actionParams = new HBox()

    lazy val removeButton = new StackPane {
      padding = Insets(0, 0, 0, 10)
      children += new Button("-") {
        onAction = { _ =>
          parentTreeItem.getChildren.remove(self)
          parentRule.actionsProperty() -= action
        }
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

    graphic = new HBox(actionTypeCombo, actionParams, removeButton)
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
      paramMap += paramName -> ParamValue(value().id.value.toString())
    }

    items <== EasyBind select story selectObject new JFunction[UiStory, javafx.beans.value.ObservableValue[ObservableList[UiNode]]] {
      override def apply(t: UiStory): property.ObjectProperty[ObservableList[UiNode]] = t.nodesProperty
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = story().nodes find (_.id.value == BigInt(paramValue.value)) foreach { n => value = n }
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
                     val story: ObjectProperty[UiStory]) extends ComboBox[UiRule] with RuleCellParameterComponent {
    cellFactory = { _ =>
      new JfxListCell[UiRule] {
        override def updateItem(item: UiRule, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }
      }
    }

    converter = new StringConverter[UiRule] {
      override def fromString(string: String): UiRule = (story().links find (_.name == string)).get

      override def toString(t: UiRule): String = t.name
    }

    onAction = { _ =>
      paramMap += paramName -> ParamValue(value().id.value.toString())
    }

    items = story().links

    story onChange { (_, _, `new`) =>
      Option(`new`) foreach { s => items = s.links }
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = story().links find (_.id.value == BigInt(paramValue.value)) foreach { r => value = r }
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
      paramMap += paramName -> ParamValue(selectionModel().getSelectedItem.id.value.toString())
    }

    items = intFacts

    story onChange { (_, _, `new`) =>
      Option(`new`) foreach { _ => items = intFacts }
    }

    def intFacts = story().factsProperty() collect { case f: IntegerFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = intFacts find (_.id.value == BigInt(paramValue.value)) foreach { f => value =  f }
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
      paramMap += paramName -> ParamValue(selectionModel().getSelectedItem.id.toString)
    }

    items = boolFacts

    story onChange { (_, _ , `new`) =>
      items = boolFacts
    }

    def boolFacts = story().factsProperty() collect { case f: BooleanFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = boolFacts find (_.id.value == BigInt(paramValue.value)) foreach { f => value =  f }
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
      paramMap += paramName -> ParamValue(selectionModel().getSelectedItem.id.toString)
    }

    items = stringFacts

    story onChange { (_, _, `new`) =>
      items = stringFacts
    }

    def stringFacts = story().factsProperty() collect { case f: StringFact => f }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = stringFacts find (_.id.value == BigInt(paramValue.value)) foreach { f => value = f }
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
    text onChange { (_, _ , newValue) =>
      Option(newValue) foreach (paramMap += paramName -> ParamValue(_))
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = text = paramValue.value
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
      editable = true
      setValue(0)

      setConverter(new StringConverter[BigInt] {
        override def fromString(string: String): BigInt = Try(BigInt(string)) getOrElse getValue

        override def toString(t: BigInt): String = t.toString()
      })

      override def increment(steps: Int): Unit = setValue(getValue + steps)

      override def decrement(steps: Int): Unit = setValue(getValue - steps)
    }

    value onChange { (_, _, newValue) =>
      Option(newValue) foreach { v => paramMap += paramName -> ParamValue(v.toString()) }
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = valueFactory().setValue(BigInt(paramValue.value))
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
      paramMap += paramName -> ParamValue(selectionModel().getSelectedItem)
    }

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = value = paramValue.value
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

        paramMap += paramName -> ParamValue(selected.name)

        pane.children += createParameterInput(selected, paramMap, story)
      }
    }

    lazy val pane = new HBox

    children.addAll(unionValue, pane)

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(string: ParamValue): Unit = params find { case (_, param) => param.name == paramName.value } foreach { p => unionValue.value = p }
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
    paramMap += paramName -> ParamValue(params map (_.name) mkString ":")

    params foreach (children += createParameterInput(_, paramMap, story))

    override def `val`: Option[ParamValue] = paramMap get paramName

    override def val_=(paramValue: ParamValue): Unit = ()
  }
}
