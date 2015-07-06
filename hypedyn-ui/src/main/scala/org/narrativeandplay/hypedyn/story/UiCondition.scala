package org.narrativeandplay.hypedyn.story

import javafx.beans.property.SimpleMapProperty

import scalafx.Includes._
import scalafx.beans.property.StringProperty

import org.narrativeandplay.hypedyn.story.rules.Conditional

class UiCondition(initConditionType: String, initParams: Map[String, String]) extends Conditional {
  val conditionTypeProperty = StringProperty(initConditionType)
  val paramsProperty = new SimpleMapProperty[String, String]()

  initParams foreach { case (k, v) =>
    paramsProperty.put(k, v)
  }

  /**
   * The type of the condition that is being instanced
   */
  override def conditionType: String = conditionTypeProperty()

  /**
   * A mapping of the condition's parameter names to the instanced values
   */
  override def params: Map[String, String] = paramsProperty.toMap

  override def toString: String = s"UiCondition($conditionType, $params)"
}

object UiCondition {
  def apply(initConditionType: String, initParams: Map[String, String]) =
    new UiCondition(initConditionType, initParams)
}
