package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.RuleLike

case class UiNodeContent(text: String, rulesets: Map[NodalContent.RulesetIndexes, RuleLike]) extends NodalContent
