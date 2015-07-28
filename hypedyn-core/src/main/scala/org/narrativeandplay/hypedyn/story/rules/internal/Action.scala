package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.story.rules.{RuleLike, Actionable}

case class Action(actionType: Actionable.ActionType,
                  params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Actionable
