package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{Control => JfxControl}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story._

class RuleCell(val rule: UiRule,
               val conditionDefs: List[ConditionDefinition],
               val actionDefs: List[ActionDefinition],
               val story: ObjectProperty[UiStory],
               val ruleList: ObservableBuffer[UiRule]) extends JfxControl {
  val cellSkin = new RuleCellSkin(this)
  setSkin(cellSkin)

  // Shrink the display when the rule is collapsed
  // However, because the height of a TreeView can't be set directly, we can only set the preferred height.
  // The problem with this is that preferred height changes don't result in actual height changes without a redraw,
  // and there is no way to force a redraw without clicking outside of the cell
  prefHeightProperty <== when (cellSkin.rootNode.expandedItemCount === 1) choose 50 otherwise 175

  def addCondition(): UiCondition = {
    val newCond = new UiCondition(ConditionType("NodeCondition"), Map.empty)
    rule.conditionsProperty += newCond
    newCond
  }

  def addAction(): UiAction = {
    val newAction = new UiAction(actionDefs.head.actionType, Map.empty)
    rule.actionsProperty += newAction
    newAction
  }
}
