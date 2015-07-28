package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.story.rules.{RuleLike, Conditional}

case class Condition(conditionType: Conditional.ConditionType,
                     params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Conditional
