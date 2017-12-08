package org.narrativeandplay.hypedyn.ui.components

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control.TreeItem.sfxTreeItemToJfx
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, VBox}

import org.narrativeandplay.hypedyn.api.story.rules.BooleanOperator.Or
import org.narrativeandplay.hypedyn.api.story.rules.{ActionDefinition, ConditionDefinition, RuleId}
import org.narrativeandplay.hypedyn.ui.story.{UiRule, UiStory}

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
  private val collapseAllButton = new Button("Collapse all") {
    onAction = { _ =>
      changeExpansionStateTo(false)
    }
  }
  private val expandAllButton = new Button("Expand all") {
    onAction = { _ =>
      changeExpansionStateTo(true)
    }
  }

  val disableAddRule = addRuleButton.disable

  children += new ToolBar {
    items.addAll(addRuleButton, collapseAllButton, expandAllButton)
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

  private def changeExpansionStateTo(expanded: Boolean): Unit = {
    def collapse(treeItem: TreeItem[_]): Unit = {
      treeItem.expanded = expanded
      treeItem.children foreach (collapse(_))
    }

    rulesList.root().children foreach (collapse(_))
  }

  VBox.setVgrow(rulesList, Priority.Always)
}
