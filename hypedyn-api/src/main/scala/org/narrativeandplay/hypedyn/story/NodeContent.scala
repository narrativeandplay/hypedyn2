package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.NodeContent.RulesetIndexes
import org.narrativeandplay.hypedyn.story.rules.Rule

sealed case class NodeContent(text: String, rulesets: Map[RulesetIndexes, Rule] = Map.empty) extends NarrativeElement[NodeContent]

object NodeContent {
  sealed case class RulesetIndexes(startIndex: TextIndex, endIndex: TextIndex)

  case class TextIndex(index: Int) extends AnyVal with Ordered[TextIndex] {
    override def compare(that: TextIndex): Int = index compare that.index
  }
}
