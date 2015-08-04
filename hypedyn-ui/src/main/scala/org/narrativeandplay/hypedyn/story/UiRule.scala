package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.story.rules._

class UiRule(val id: RuleId,
             initName: String,
             initConditionsOp: BooleanOperator,
             initConditions: List[UiCondition],
             initActions: List[UiAction]) extends RuleLike {
  val nameProperty = StringProperty(initName)
  val conditionsOpProperty = ObjectProperty(initConditionsOp)
  val conditionsProperty = ObjectProperty(ObservableBuffer(initConditions: _*))
  val actionsProperty = ObjectProperty(ObservableBuffer(initActions: _*))

  override def name: String = nameProperty()

  override def conditionsOp: BooleanOperator = conditionsOpProperty()

  override def actions: List[UiAction] = actionsProperty().toList

  override def conditions: List[UiCondition] = conditionsProperty().toList

  override def toString: String = s"UiRule($id, $name, $conditionsOp, $conditions, $actions)"
}
