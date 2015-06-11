package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.NodalContent
import org.narrativeandplay.hypedyn.story.NodalContent.RulesetIndexes
import org.narrativeandplay.hypedyn.story.rules.internal.{Condition, Rule}

case class NodeContent(text: String, rulesets: Map[RulesetIndexes, Rule]) extends NodalContent
