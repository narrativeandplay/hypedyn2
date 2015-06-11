package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.story.rules.{BooleanOperator, RuleId, RuleLike}

case class Rule(id: RuleId,
                name: String,
                conditionsOp: BooleanOperator,
                conditions: List[Condition],
                actions: List[Action]) extends RuleLike
