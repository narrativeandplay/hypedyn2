package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableMap

import org.narrativeandplay.hypedyn.story.rules.Actionable
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}

class UiAction(initActionType: ActionType, initParams: Map[ParamName, ParamValue]) extends Actionable {
  val actionTypeProperty = ObjectProperty(initActionType)
  val paramsProperty = ObjectProperty(ObservableMap(initParams.toSeq: _*))

  override def actionType: ActionType = actionTypeProperty()

  override def params: Map[ParamName, ParamValue] = paramsProperty().toMap

  override def toString: String = s"UiAction($actionType, $params)"
}
