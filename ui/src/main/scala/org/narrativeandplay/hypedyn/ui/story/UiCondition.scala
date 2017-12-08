package org.narrativeandplay.hypedyn.ui.story

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableMap

import org.narrativeandplay.hypedyn.api.story.rules.Conditional
import org.narrativeandplay.hypedyn.api.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.api.utils.PrettyPrintable

/**
 * UI implementation for Conditional
 *
 * @param initConditionType The initial condition type
 * @param initParams The initial paramters and their values
 */
class UiCondition(initConditionType: ConditionType, initParams: Map[ParamName, ParamValue])
  extends Conditional
  with PrettyPrintable {
  /**
   * Backing property for the condition type
   */
  val conditionTypeProperty = ObjectProperty(initConditionType)

  /**
   * Backing property for the paramters and their values
   */
  val paramsProperty = ObjectProperty(ObservableMap(initParams.toSeq: _*))

  /**
   * The type of the condition that is being instanced
   */
  override def conditionType: ConditionType = conditionTypeProperty()

  /**
   * A mapping of the condition's parameter names to the instanced values
   */
  override def params: Map[ParamName, ParamValue] = paramsProperty().toMap

  override def toString: String = {
    val fields = List("conditionType" -> conditionType, "params" -> params)
    val doc = list(fields, getClass.getSimpleName, any)
    pretty(doc).layout
  }
}

object UiCondition {
  def apply(initConditionType: ConditionType, initParams: Map[ParamName, ParamValue]) =
    new UiCondition(initConditionType, initParams)
}
