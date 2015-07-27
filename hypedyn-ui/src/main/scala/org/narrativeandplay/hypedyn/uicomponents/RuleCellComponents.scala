package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{SpinnerValueFactory => JfxSpinnerValueFactory, ListCell => JfxListCell}

import scala.util.Try

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.{ObservableBuffer, ObservableMap}
import scalafx.scene.control.{Spinner, TextArea, ComboBox, TreeItem}
import scalafx.scene.layout.{Region, HBox}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx

import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.utils.ScalaJavaImplicits._

object RuleCellComponents {
  sealed trait RuleCellParameterComponent {
    def story: ObjectProperty[UiStory]
    def paramName: String
    def paramMap: ObservableMap[String, String]
    def `val`: Option[String]
    def val_=(string: String): Unit
  }
  
  class ConditionCell(val condition: UiCondition, 
                      val conditionDefinitions: List[ConditionDefinition],
                      val parentStory: ObjectProperty[UiStory]) extends TreeItem[String]("") {
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
        condition.paramsProperty.clear()
        condition.conditionTypeProperty() = selectionModel().getSelectedItem.conditionName

        condParams.children.clear()
        condParamsChildren.clear()

        generateParamInputComponents()
      }

      value = (conditionDefinitions find (_.conditionName == condition.conditionType)).get
      selectionModel().getSelectedItem.parameters foreach { p =>
        val newComponent = createParameterInput(p, condition.paramsProperty, parentStory)
        condParams.children += newComponent
        condParamsChildren += newComponent
      }
      condParamsChildren foreach { component =>
        condition.params get component.paramName foreach { v => component.`val` = v }
      }
    }

    lazy val condParams = new HBox()

    private def generateParamInputComponents(): Unit = condTypeCombo.selectionModel().getSelectedItem.parameters foreach { p =>
      val newComponent = createParameterInput(p, condition.paramsProperty, parentStory)
      condParams.children += newComponent
      condParamsChildren += newComponent
    }

    graphic = new HBox(condTypeCombo, condParams)
    
  }

  class ActionCell(val action: UiAction,
                   val actionDefinitions: List[ActionDefinition],
                   val parentStory: ObjectProperty[UiStory]) extends TreeItem[String]("") {
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
        action.paramsProperty.clear()
        action.actionTypeProperty() = selectionModel().getSelectedItem.actionName

        actionParams.children.clear()
        actionParamsChildren.clear()

        generateParameterInputComponents(this)
      }

      value = (actionDefinitions find (_.actionName == action.actionType)).get
      generateParameterInputComponents(this)
      actionParamsChildren foreach { component =>
        action.params get component.paramName foreach { v => component.`val` = v }
      }
    }

    lazy val actionParams = new HBox()

    private def generateParameterInputComponents(comboBox: ComboBox[ActionDefinition]) =
      comboBox.selectionModel().getSelectedItem.parameters foreach { p =>
        val newComponent = createParameterInput(p, action.paramsProperty, parentStory)
        actionParams.children += newComponent
        actionParamsChildren += newComponent
      }

    graphic = new HBox(actionTypeCombo, actionParams)
  }

  private def createParameterInput(ruleParameter: RuleParameter,
                                   parameterMap: ObservableMap[String, String],
                                   parentStory: ObjectProperty[UiStory]) = ruleParameter.possibleValues match {
    case Nodes => new NodeComboBox(parameterMap, ruleParameter.name, parentStory)
    case Links => new LinkComboBox(parameterMap, ruleParameter.name, parentStory)
    case IntegerFacts => new IntegerFactsComboBox(parameterMap, ruleParameter.name, parentStory)
    case BooleanFacts => new BooleanFactsComboBox(parameterMap, ruleParameter.name, parentStory)
    case StringFacts => new StringFactsComboBox(parameterMap, ruleParameter.name, parentStory)
    case UserInputString => new StringInput(parameterMap, ruleParameter.name, parentStory)
    case UserInputInteger => new IntegerInput(parameterMap, ruleParameter.name, parentStory)
    case ListOfValues(vals @ _*) => new ValuesListComboBox(parameterMap, ruleParameter.name, parentStory, vals)
    case Union(params) => new UnionComboBoxPane(parameterMap, ruleParameter.name, parentStory, params)
    case Product(params) => new ProductPane(parameterMap, ruleParameter.name, parentStory, params)
  }

  class NodeComboBox(val paramMap: ObservableMap[String, String],
                     val paramName: String,
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
      paramMap += paramName -> value().id.value.toString
    }

    items = story().nodesProperty


    story onChange { (_, _, `new`) =>
      Option(`new`) foreach { s => items = s.nodesProperty }
    }

    override def `val`: Option[String] = Option(value()) map (_.id.value.toString())

    override def val_=(string: String): Unit = story().nodes find (_.id.value == BigInt(string)) foreach { n => value = n }
  }

  class LinkComboBox(val paramMap: ObservableMap[String, String],
                     val paramName: String,
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
      paramMap += paramName -> value().id.value.toString()
    }

    items = story().links

    story onChange { (_, _, `new`) =>
      Option(`new`) foreach { s => items = s.links }
    }

    override def `val`: Option[String] = Option(value()) map (_.id.value.toString())

    override def val_=(string: String): Unit = story().links find (_.id.value == BigInt(string)) foreach { r => value = r }
  }

  class IntegerFactsComboBox(val paramMap: ObservableMap[String, String],
                             val paramName: String,
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
      paramMap += paramName -> selectionModel().getSelectedItem.id.value.toString()
    }

    items = intFacts

    story onChange { (_, _, `new`) =>
      Option(`new`) foreach { _ => items = intFacts }
    }

    def intFacts = story().factsProperty collect { case f: IntegerFact => f }

    override def `val`: Option[String] = Option(value()) map (_.id.value.toString())

    override def val_=(string: String): Unit = intFacts find (_.id.value == BigInt(string)) foreach { f => value =  f }
  }

  class BooleanFactsComboBox(val paramMap: ObservableMap[String, String],
                             val paramName: String,
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
      paramMap += paramName -> selectionModel().getSelectedItem.id.toString
    }

    items = boolFacts

    story onChange { (_, _ , `new`) =>
      items = boolFacts
    }

    def boolFacts = story().factsProperty collect { case f: BooleanFact => f }

    override def `val`: Option[String] = Option(value()) map (_.id.value.toString())

    override def val_=(string: String): Unit = boolFacts find (_.id.value == BigInt(string)) foreach { f => value =  f }
  }

  class StringFactsComboBox(val paramMap: ObservableMap[String, String],
                            val paramName: String,
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
      paramMap += paramName -> selectionModel().getSelectedItem.id.toString
    }

    items = stringFacts

    story onChange { (_, _, `new`) =>
      items = stringFacts
    }

    def stringFacts = story().factsProperty collect { case f: StringFact => f }

    override def `val`: Option[String] = Option(value()) map (_.id.value.toString())

    override def val_=(string: String): Unit = stringFacts find (_.id.value == BigInt(string)) foreach { f => value = f }
  }

  class StringInput(val paramMap: ObservableMap[String, String],
                    val paramName: String,
                    val story: ObjectProperty[UiStory]) extends TextArea with RuleCellParameterComponent {
    text onChange { (_, _ , newValue) =>
      Option(newValue) foreach (paramMap += paramName -> _)
    }

    override def `val`: Option[String] = Option(text())

    override def val_=(string: String): Unit = text = string
  }

  class IntegerInput(val paramMap: ObservableMap[String, String],
                     val paramName: String,
                     val story: ObjectProperty[UiStory]) extends Spinner[BigInt] with RuleCellParameterComponent {
    valueFactory = new JfxSpinnerValueFactory[BigInt] {
      editable = true
      setValue(0)

      setConverter(new StringConverter[BigInt] {
        override def fromString(string: String): BigInt = Try(BigInt(string)) getOrElse BigInt(0)

        override def toString(t: BigInt): String = t.toString()
      })

      override def increment(steps: Int): Unit = setValue(getValue + steps)

      override def decrement(steps: Int): Unit = setValue(getValue - steps)
    }

    value onChange { (_, _, newValue) =>
      Option(newValue) foreach (paramMap += paramName -> _.toString())
    }

    override def `val`: Option[String] = Option(value()) map (_.toString())

    override def val_=(string: String): Unit = valueFactory().setValue(BigInt(string))
  }

  class ValuesListComboBox(val paramMap: ObservableMap[String, String],
                           val paramName: String,
                           val story: ObjectProperty[UiStory],
                           values: Seq[String]) extends ComboBox[String] with RuleCellParameterComponent {
    items = ObservableBuffer(values)

    onAction = { _ =>
      paramMap += paramName -> selectionModel().getSelectedItem
    }

    override def `val`: Option[String] = Option(value())

    override def val_=(string: String): Unit = value = string
  }

  class UnionComboBoxPane(val paramMap: ObservableMap[String, String],
                          val paramName: String,
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

        paramMap += paramName -> selected.name

        pane.children += createParameterInput(selected, paramMap, story)
      }
    }

    lazy val pane = new HBox

    children.addAll(unionValue, pane)

    override def `val`: Option[String] = Option(unionValue.value()) map (_._2.name)

    override def val_=(string: String): Unit = params find (_._2.name == string) foreach { p => unionValue.value = p }
  }

  class ProductPane(val paramMap: ObservableMap[String, String],
                    val paramName: String,
                    val story: ObjectProperty[UiStory],
                    params: List[RuleParameter]) extends HBox with RuleCellParameterComponent {
    paramMap += paramName -> (params map (_.name) mkString ":")

    params foreach (children += createParameterInput(_, paramMap, story))

    override def `val`: Option[String] = paramMap get paramName

    override def val_=(string: String): Unit = {}
  }
}
