package org.narrativeandplay.hypedyn.api.story

import org.narrativeandplay.hypedyn.api.story.rules.RuleLike

/**
 * An interface for the content of a node
 */
trait NodalContent extends NarrativeElement[NodalContent] {
  /**
   * Returns the text of the node
   */
  def text: String

  /**
   * Returns the list of text rules of the node
   */
  def rulesets: List[NodalContent.RulesetLike]
}

object NodalContent {

  /**
   * An interface for a text rule
   *
   * A text rule is a list of rules attached to a single piece of text
   */
  trait RulesetLike {
    /**
     * Returns the ID of the text rule
     */
    def id: RulesetId

    /**
     * Returns the name of the text rule
     */
    def name: String

    /**
     * Returns the index range in the rule text where this text rule applies
     */
    def indexes: RulesetIndexes

    /**
     * Returns the list of rules that are in this text rule
     */
    def rules: List[RuleLike]
  }

  /**
   * Class representing an index range on the text of a node; indexes start from 0, and are inclusive
   *
   * @param startIndex The start index of the range
   * @param endIndex The end index of a range
   */
  sealed case class RulesetIndexes(startIndex: TextIndex, endIndex: TextIndex)

  /**
   * Value class of an index in a piece of text
   *
   * @param index The integer value of the index
   */
  case class TextIndex(index: BigInt) extends AnyVal with Ordered[TextIndex] {
    override def compare(that: TextIndex): Int = index compare that.index
  }

  /**
   * Value type for the ID of a ruleset
   *
   * @param value The integer value of the ID
   */
  case class RulesetId(value: BigInt) extends AnyVal with Ordered[RulesetId] {
    override def compare(that: RulesetId): Int = value compare that.value

    /**
     * Returns a ruleset ID which has it's value incremented by one from the original
     */
    def increment = new RulesetId(value + 1)

    /**
     * An alias for `increment`
     */
    def inc = increment

    /**
     * Returns a ruleset ID which has it's value decremented by one from the original
     */
    def decrement = new RulesetId(value - 1)

    /**
     * An alias for `decrement`
     */
    def dec = decrement

    /**
     * Returns true if the ruleset ID is valid, false otherwise
     *
     * A valid ruleset ID is one whose value is greater than or equal to 0
     */
    def isValid = value >= 0
  }
}
