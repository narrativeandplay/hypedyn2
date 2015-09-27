package org.narrativeandplay.hypedyn.uicomponents

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, TreeItem, TreeView}
import scalafx.scene.layout.{Priority, HBox, VBox}
import scalafx.scene.Parent.sfxParent2jfx

import org.narrativeandplay.hypedyn.story.rules.BooleanOperator.Or
import org.narrativeandplay.hypedyn.story.{UiStory, UiRule}
import org.narrativeandplay.hypedyn.story.rules.{RuleId, ActionDefinition, ConditionDefinition}

/**
 * A control to manipulate rules
 *
 * @param labelText The heading text of the pane
 * @param conditionDefinitions The list of condition definitions
 * @param actionDefinitions The list of action definitions
 * @param initRules The initial list of rules for the control
 * @param story The story the list of rules belongs to
 */
class RulesPane(labelText: String,
                val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                initRules: ObservableBuffer[UiRule],
                val story: ObjectProperty[UiStory]) extends VBox(5) {
  prefHeight = 200

  val rules = ObjectProperty(initRules)
  private val self = this

  private val rulesList = new TreeView[String](new TreeItem[String]("")) {
    showRoot = false

    selectionModel().selectedItem onChange {
      Platform runLater { selectionModel().clearSelection() }
    }
  }

  private val label = new Label(labelText)
  private val addRuleButton = new Button("Add rule") {
    // 73 is the minimum width of the button at runtime that shows the full button label; allowing a fully automatic
    // computation causes the computed width to be less than this value at times, so we force to button to be minimally
    // this long to ensure that the full label always gets displayed
    minWidth = 73

    onAction = { _ =>
      val newRule = new UiRule(RuleId(-1), "New Rule", false, Or, Nil, Nil)
      rules() += newRule
      rulesList.root().children += new RuleCell(newRule, conditionDefinitions, actionDefinitions, story, rules(), self)
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
  children += rulesList

  // Initialise cells
  rules() foreach { rule =>
    rulesList.root().children += new RuleCell(rule, conditionDefinitions, actionDefinitions, story, rules(), this)
  }

  // Redraw the whole pane when the whole buffer changes
  rules onChange { (_, _, currentRules) =>
    rulesList.root().children.clear()
    currentRules foreach { rule =>
      rulesList.root().children += new RuleCell(rule, conditionDefinitions, actionDefinitions, story, rules(), this)
    }
  }

  def rearrangeCells(): Unit = {
    rulesList.root().children.clear()
    rules() foreach { rule =>
      rulesList.root().children += new RuleCell(rule, conditionDefinitions, actionDefinitions, story, rules(), this)
    }
  }

  VBox.setVgrow(rulesList, Priority.Always)
}
