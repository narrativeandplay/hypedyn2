package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.RuleLike

trait NodalContent extends NarrativeElement[NodalContent] {
  def text: String
  def rulesets: List[NodalContent.RulesetLike]
}

object NodalContent {
  trait RulesetLike {
    def id: RulesetId
    def name: String
    def indexes: RulesetIndexes
    def rules: List[RuleLike]
  }
  
  sealed case class RulesetIndexes(startIndex: TextIndex, endIndex: TextIndex)

  case class TextIndex(index: BigInt) extends AnyVal with Ordered[TextIndex] {
    override def compare(that: TextIndex): Int = index compare that.index
  }

  case class RulesetId(value: BigInt) extends AnyVal with Ordered[RulesetId] {
    override def compare(that: RulesetId): Int = value compare that.value

    def increment = new RulesetId(value + 1)
    def inc = increment

    def decrement = new RulesetId(value - 1)
    def dec = decrement

    def isValid = value >= 0
  }
}
