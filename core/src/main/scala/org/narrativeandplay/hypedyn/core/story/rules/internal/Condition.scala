package org.narrativeandplay.hypedyn.core.story.rules.internal

import org.narrativeandplay.hypedyn.api.story.rules.{RuleLike, Conditional}

/**
 * Class representing a condition instance
 *
 * @param conditionType The type of the instanced condition
 * @param params The parameters and values of those parameters for the instanced condition
 */
case class Condition(conditionType: Conditional.ConditionType,
                     params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Conditional
