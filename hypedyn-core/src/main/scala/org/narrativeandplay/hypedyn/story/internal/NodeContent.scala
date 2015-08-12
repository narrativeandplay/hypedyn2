package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.NodalContent
import org.narrativeandplay.hypedyn.story.rules.internal.Rule

/**
 * Class for representing the content of a node
 *
 * @param text The text of the node
 * @param rulesets The list of text rules of the node
 */
case class NodeContent(text: String, rulesets: List[NodeContent.Ruleset]) extends NodalContent

object NodeContent {

  /**
   * Class representing a single text rule
   *
   * @param id The id of the text rule
   * @param name The name of the text rule
   * @param indexes The location in the text where this rule applies
   * @param rules The list of rules in this text rule
   */
  case class Ruleset(id: NodalContent.RulesetId,
                     name: String,
                     indexes: NodalContent.RulesetIndexes,
                     rules: List[Rule]) extends NodalContent.RulesetLike
}
