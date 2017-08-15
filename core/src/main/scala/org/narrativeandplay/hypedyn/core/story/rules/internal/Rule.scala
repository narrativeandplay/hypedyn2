package org.narrativeandplay.hypedyn.core.story.rules.internal

import org.narrativeandplay.hypedyn.api.story.rules.{BooleanOperator, RuleId, RuleLike}

/**
 * Class representing a rule
 *
 * @param id The ID of the rule
 * @param name The name of the rule
 * @param stopIfTrue Whether further rules should execute when this rule is true
 * @param conditionsOp How to combine the result of multiple condition evaluations
 * @param conditions The conditions of the rule
 * @param actions The actions of the rule
 */
case class Rule(id: RuleId,
                name: String,
                stopIfTrue: Boolean,
                conditionsOp: BooleanOperator,
                conditions: List[Condition],
                actions: List[Action]) extends RuleLike
