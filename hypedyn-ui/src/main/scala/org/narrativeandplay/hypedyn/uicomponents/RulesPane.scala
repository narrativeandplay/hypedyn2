package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{ListCell => JFXListCell}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Pos, Insets}
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, HBox, VBox}
import scalafx.scene.Parent.sfxParent2jfx

import org.narrativeandplay.hypedyn.story.rules.BooleanOperator.Or
import org.narrativeandplay.hypedyn.story.{UiStory, UiRule}
import org.narrativeandplay.hypedyn.story.rules._

class RulesPane(labelText: String,
                val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                initRules: ObservableBuffer[UiRule],
                val story: ObjectProperty[UiStory]) extends VBox(5) {
  prefHeight = 200

  val rules = ObjectProperty(initRules)
  private val self = this

  private val ruleList = new ListView[UiRule]() {
    items <== rules

    cellFactory = { _ =>
      new RulesPane.RulesPaneCell(self)
    }
  }
  private val label = new Label(labelText)
  private val addRuleButton = new Button("Add rule") {
    // 73 is the minimum width of the button at runtime that shows the full button label; allowing a fully automatic
    // computation causes the computed width to be less than this value at times, so we force to button to be minimally
    // this long to ensure that the full label always gets displayed
    minWidth = 73

    onAction = { _ =>
      rules() += new UiRule(RuleId(-1), "New Rule", false, Or, Nil, Nil)
    }
  }

  val disableAddRule = addRuleButton.disable

  children += new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(0, 5, 0, 5)

    children += label
    children += addRuleButton

    label.prefWidth <== width - addRuleButton.width - padding().left - padding().right
  }
  children += ruleList

  VBox.setVgrow(ruleList, Priority.Always)
}

object RulesPane {
  private class RulesPaneCell(parentView: RulesPane) extends JFXListCell[UiRule] {
    setPadding(Insets.Empty) // fill the whole cell
    private val self = this

    override def updateItem(item: UiRule, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      if (empty || item == null) {
        setGraphic(null)
      }
      else {
        val cell = new RuleCell(item, parentView.conditionDefinitions, parentView.actionDefinitions, parentView.story, parentView.rules())
        setGraphic(cell)
      }
    }
  }
}
