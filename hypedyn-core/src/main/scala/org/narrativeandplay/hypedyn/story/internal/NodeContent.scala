package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.NodalContent
import org.narrativeandplay.hypedyn.story.rules.internal.Rule

case class NodeContent(text: String, rulesets: List[NodeContent.Ruleset]) extends NodalContent

object NodeContent {
  case class Ruleset(id: NodalContent.RulesetId,
                     name: String,
                     indexes: NodalContent.RulesetIndexes,
                     rules: List[Rule]) extends NodalContent.RulesetLike
}
