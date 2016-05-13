package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.story.NodalContent
import org.narrativeandplay.hypedyn.story.rules.internal.Rule

/**
 * Class for representing the content of a node
 *
 * @param text The text of the node
 * @param rulesets The list of text rules of the node
 */
case class NodeContent(text: String, rulesets: List[NodeContent.Ruleset]) extends NodalContent {
  /**
   * Returns the list of segments of the node
   */
  override def segments: List[(String, Option[NodeContent.Ruleset])] = {
    // See http://stackoverflow.com/questions/37091135/inserting-missing-values-into-a-list/37092356#37092356
    val triples = rulesets map { ruleset =>
      (ruleset.indexes.startIndex.index, ruleset.indexes.endIndex.index, Option(ruleset))
    } sortBy (_._1)
    val xs = (BigInt(0), BigInt(0), None) :: (triples :+ ((BigInt(text.length), BigInt(text.length), None)))

    val ys = xs sliding 2 flatMap { case List(fst @ (_, end1, _), (start2, _, _)) =>
      if (end1 != start2) List(fst, (end1, start2, None)) else List(fst)
    }
    val filledInRulesets = ys.toList.tail

    filledInRulesets map { case (start, end, rulesetOption) =>
      (text substring (start.intValue(), end.intValue()), rulesetOption)
    }
  }
}

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
