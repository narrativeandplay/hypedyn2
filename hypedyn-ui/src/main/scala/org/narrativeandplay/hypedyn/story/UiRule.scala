package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.story.rules._

/**
 * UI implementation of RuleLike
 *
 * @param id The ID of the rule
 * @param initName The initial name of the rule
 * @param initStopIfTrue The initial value of whether further rules in the list should execute if this does
 * @param initConditionsOp The initial operator for combining the conditions of the rule
 * @param initConditions The initial conditions of the rule
 * @param initActions The initial actions of the rule
 */
class UiRule(val id: RuleId,
             initName: String,
             initStopIfTrue: Boolean,
             initConditionsOp: BooleanOperator,
             initConditions: List[UiCondition],
             initActions: List[UiAction]) extends RuleLike {
  /**
   * Backing property for the name
   */
  val nameProperty = StringProperty(initName)

  /**
   * Backing property for `stopIfTrue`
   */
  val stopIfTrueProperty = BooleanProperty(initStopIfTrue)

  /**
   * Backing property for the operator for combining conditions
   */
  val conditionsOpProperty = ObjectProperty(initConditionsOp)

  /**
   * Backing property for the conditions
   */
  val conditionsProperty = ObjectProperty(ObservableBuffer(initConditions: _*))

  /**
   * Backing property for the actions
   */
  val actionsProperty = ObjectProperty(ObservableBuffer(initActions: _*))

  /**
   * Returns the name of the rule
   */
  override def name: String = nameProperty()

  /**
   * Returns true if the execution of further rules is to be stopped if this rule executes, false otherwise
   */
  override def stopIfTrue: Boolean = stopIfTrueProperty()

  /**
   * Returns the boolean operator that is to be used when evaluating a set of conditions
   */
  override def conditionsOp: BooleanOperator = conditionsOpProperty()

  /**
   * Returns the list of actions of the rule
   */
  override def actions: List[UiAction] = actionsProperty().toList

  /**
   * Returns the list of conditions of the rule
   */
  override def conditions: List[UiCondition] = conditionsProperty().toList

  override def toString: String = s"UiRule($id, $name, $conditionsOp, $conditions, $actions)"
}
