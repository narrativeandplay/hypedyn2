package org.narrativeandplay.hypedyn.uicomponents

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, TreeItem, TreeView}
import scalafx.scene.layout.{Priority, HBox, VBox}
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control.TreeItem.sfxTreeItemToJfx

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
    onAction = { _ =>
      val newRule = new UiRule(RuleId(-1), "New Rule", false, Or, Nil, Nil)
      rules() += newRule
      rulesList.root().children += new RuleCell(newRule, conditionDefinitions, actionDefinitions, story, rules(), self)
    }
  }

  val disableAddRule = addRuleButton.disable

  children += new HBox {
    alignment = Pos.CenterLeft
    padding = Insets(5, 5, 0, 5)

    children += label
    children += new HBox { HBox.setHgrow(this, Priority.Always) } // Add expandable empty space to push the add button
                                                                  // to the end
    children += addRuleButton
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

  /**
   * Redraw rule cells when rule positions are swapped
   *
   * @param swappedRulePositions A pair contains the positions of which rules were swapped
   */
  def rearrangeCells(swappedRulePositions: (Int, Int)): Unit = {
    val expandedStates = rulesList.root().children map (_.isExpanded)
    swappedRulePositions match { case (first, second) =>
      val firstExpandedState = expandedStates(first)
      val secondExpandedState = expandedStates(second)
      expandedStates.set(first, secondExpandedState)
      expandedStates.set(second, firstExpandedState)
    }

    rulesList.root().children.clear()
    rules() zip expandedStates foreach { case (rule, expand) =>
      rulesList.root().children += new RuleCell(rule, conditionDefinitions, actionDefinitions, story, rules(), this) {
        expanded = expand
      }
    }
  }

  VBox.setVgrow(rulesList, Priority.Always)
}
