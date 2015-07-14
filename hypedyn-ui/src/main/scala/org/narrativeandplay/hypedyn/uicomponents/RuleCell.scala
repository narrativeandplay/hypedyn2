package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{Control => JfxControl, SpinnerValueFactory => JfxSpinnerValueFactory, ListCell => JfxListCell}

import scala.util.Try

import scalafx.Includes._
import scalafx.collections.{ObservableMap, ObservableBuffer}
import scalafx.scene.control.{ComboBox, TreeItem, Spinner, TextArea}
import scalafx.scene.layout.HBox
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx

import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.{UiCondition, Narrative, Nodal, UiRule}

class RuleCell(val rule: UiRule,
               val conditionDefs: List[ConditionDefinition],
               val actionDefs: List[ActionDefinition],
               val story: Narrative) extends JfxControl {
  setSkin(new RuleCellSkin(this))
}

object RuleCell {
  class ConditionCell(val condition: UiCondition,
                      val conditionDefs: List[ConditionDefinition],
                      story: Narrative) extends TreeItem[String]("") {
    val condTypeCombo = new ComboBox[ConditionDefinition] {
      items = ObservableBuffer(conditionDefs)

      converter = new StringConverter[ConditionDefinition] {
        override def fromString(string: String): ConditionDefinition = (conditionDefs find (_.description == string)).get

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

        selectionModel().getSelectedItem.parameters foreach { p =>
          condParams.children += createParameterInput(p, condition.paramsProperty, story)
        }
      }

      value = (conditionDefs find (_.conditionName == condition.conditionType)).get
    }

    lazy val condParams = new HBox()

    graphic = new HBox(condTypeCombo, condParams)
  }

  def createParameterInput(ruleParameter: RuleParameter,
                           paramsMap: ObservableMap[String, String],
                           story: Narrative) = ruleParameter.possibleValues match {
    case Nodes => new NodeComboBox(paramsMap, ruleParameter.name, story.nodes)
    case Links => new LinkComboBox(paramsMap, ruleParameter.name)
    case IntegerFacts =>
      new IntegerFactsComboBox(paramsMap, ruleParameter.name, story.facts collect { case f: IntegerFact => f})
    case BooleanFacts =>
      new BooleanFactsComboBox(paramsMap, ruleParameter.name, story.facts collect { case f: BooleanFact => f })
    case StringFacts =>
      new StringFactsComboBox(paramsMap, ruleParameter.name, story.facts collect { case f: StringFact => f})
    case UserInputString => new StringInput(paramsMap, ruleParameter.name)
    case UserInputInteger => new IntegerInput(paramsMap, ruleParameter.name)
    case ListOfValues(vals @ _*) => new ValuesListComboBox(paramsMap, ruleParameter.name, vals)
    case Union(params) => new UnionComboBoxPane(paramsMap, ruleParameter.name, params, story)
    case Product(params) => new ProductPane(paramsMap, ruleParameter.name, params, story)
  }

  class NodeComboBox(paramMap: ObservableMap[String, String],
                     paramName: String,
                     nodes: List[Nodal]) extends ComboBox[Nodal] {
    cellFactory = { _ =>
      new JfxListCell[Nodal] {
        override def updateItem(item: Nodal, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          if (!empty && item != null) {
            setText(item.name)
          }
        }

        converter = new StringConverter[Nodal] {
          override def fromString(string: String): Nodal = (nodes find (_.name == string)).get

          override def toString(t: Nodal): String = t.name
        }
      }
    }

    onAction = { _ =>
      paramMap += paramName -> value().id.value.toString
    }

    items = ObservableBuffer(nodes)
  }

  class LinkComboBox(val paramMap: ObservableMap[String, String], val paramName: String) extends ComboBox[String]

  class IntegerFactsComboBox(paramMap: ObservableMap[String, String],
                             paramName: String,
                             intFacts: List[IntegerFact]) extends ComboBox[IntegerFact] {
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
      paramMap += paramName -> selectionModel().getSelectedItem.id.toString
    }

    items = ObservableBuffer(intFacts)
  }

  class BooleanFactsComboBox(paramMap: ObservableMap[String, String],
                             paramName: String,
                             boolFacts: List[BooleanFact]) extends ComboBox[BooleanFact] {
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
  }

  class StringFactsComboBox(paramMap: ObservableMap[String, String],
                            paramName: String,
                            stringFacts: List[StringFact]) extends ComboBox[StringFact] {
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
  }

  class StringInput(paramMap: ObservableMap[String, String], paramName: String) extends TextArea {
    text onChange { (_, _ , newValue) =>
      Option(newValue) foreach (paramMap += paramName -> _)
    }
  }

  class IntegerInput(paramMap: ObservableMap[String, String], paramName: String) extends Spinner[BigInt] {
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
  }

  class ValuesListComboBox(paramMap: ObservableMap[String, String],
                           paramName: String,
                           values: Seq[String]) extends ComboBox[String] {
    items = ObservableBuffer(values)

    onAction = { _ =>
      paramMap += paramName -> selectionModel().getSelectedItem
    }

  }

  class UnionComboBoxPane(paramMap: ObservableMap[String, String],
                          paramName: String,
                          params: Map[String, RuleParameter],
                          story: Narrative) extends HBox {
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

  }
  class ProductPane(paramMap: ObservableMap[String, String],
                    paramName: String,
                    params: List[RuleParameter],
                    story: Narrative) extends HBox {
    paramMap += paramName -> (params map (_.name) mkString ":")

    params foreach (children += createParameterInput(_, paramMap, story))
  }
}
