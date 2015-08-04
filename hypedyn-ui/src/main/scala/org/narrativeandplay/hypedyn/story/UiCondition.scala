package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableMap

import org.narrativeandplay.hypedyn.story.rules.Conditional
import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}

class UiCondition(initConditionType: ConditionType, initParams: Map[ParamName, ParamValue]) extends Conditional {
  val conditionTypeProperty = ObjectProperty(initConditionType)
  val paramsProperty = ObjectProperty(ObservableMap(initParams.toSeq: _*))

  /**
   * The type of the condition that is being instanced
   */
  override def conditionType: ConditionType = conditionTypeProperty()

  /**
   * A mapping of the condition's parameter names to the instanced values
   */
  override def params: Map[ParamName, ParamValue] = paramsProperty().toMap

  override def toString: String = s"UiCondition($conditionType, $params)"
}

object UiCondition {
  def apply(initConditionType: ConditionType, initParams: Map[ParamName, ParamValue]) =
    new UiCondition(initConditionType, initParams)
}
