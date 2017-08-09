package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.api.story.rules.{RuleLike, Actionable}

/**
 * Class for representing an action instance
 *
 * @param actionType The type of the instanced action
 * @param params The parameters and values of those parameters for the instanced action
 */
case class Action(actionType: Actionable.ActionType,
                  params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Actionable
