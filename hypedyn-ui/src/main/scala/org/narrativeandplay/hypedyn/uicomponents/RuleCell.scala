package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{Control => JfxControl}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty

import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story._

class RuleCell(val rule: UiRule,
               val conditionDefs: List[ConditionDefinition],
               val actionDefs: List[ActionDefinition],
               val story: ObjectProperty[UiStory]) extends JfxControl {
  setSkin(new RuleCellSkin(this))

  def addCondition(): UiCondition = {
    val newCond = new UiCondition("NodeCondition", Map.empty)
    rule.conditionsProperty += newCond
    newCond
  }

  def addAction(): UiAction = {
    val newAction = new UiAction(actionDefs.head.actionName, Map.empty)
    rule.actionsProperty += newAction
    newAction
  }
}
