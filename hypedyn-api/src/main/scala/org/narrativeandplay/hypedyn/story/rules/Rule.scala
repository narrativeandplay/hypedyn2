package org.narrativeandplay.hypedyn.story.rules

import org.narrativeandplay.hypedyn.story.Nodal

trait Rule[T] {
  def parent: T
  def triggerTiming: RuleTriggerTiming[T]
  def conditions: List[Condition]
  def actions: List[Action]
}

sealed trait RuleTriggerTiming[T]
case object NodeEntry extends RuleTriggerTiming[Nodal]
case object NodeExit extends RuleTriggerTiming[Nodal]
