package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableMap

import org.narrativeandplay.hypedyn.story.rules.Actionable

class UiAction(initActionType: String, initParams: Map[String, String]) extends Actionable {
  val actionTypeProperty = StringProperty(initActionType)
  val paramsProperty = ObservableMap(initParams.toSeq: _*)

  override def actionType: String = actionTypeProperty()

  override def params: Map[String, String] = paramsProperty.toMap

  override def toString: String = s"UiAction($actionType, $params)"
}
