package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.NodalContent.RulesetIndexes
import org.narrativeandplay.hypedyn.story.rules.RuleLike

trait NodalContent extends NarrativeElement[NodalContent] {
  def text: String
  def rulesets: Map[RulesetIndexes, RuleLike]
}

object NodalContent {
  sealed case class RulesetIndexes(startIndex: TextIndex, endIndex: TextIndex)

  case class TextIndex(index: Int) extends AnyVal with Ordered[TextIndex] {
    override def compare(that: TextIndex): Int = index compare that.index
  }
}
