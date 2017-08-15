package org.narrativeandplay.hypedyn.api.story.rules

/**
 * An interface for a fact
 */
sealed trait Fact {
  /**
   * Returns the ID of the fact
   */
  def id: FactId

  /**
   * Returns the name of the fact
   */
  def name: String
}

object Fact {
  val IntegerFact = "Number fact"
  val StringFact = "Text fact"
  val BooleanFact = "True/false fact"
  val IntegerFactList = "Number fact list"
  val StringFactList = "Text fact list"
  val BooleanFactList = "True/false fact list"

  val EnabledFacts = List(IntegerFact, StringFact, BooleanFact)
}

sealed case class IntegerFact(id: FactId, name: String, initalValue: BigInt) extends Fact
sealed case class StringFact(id: FactId, name: String, initialValue: String) extends Fact
sealed case class BooleanFact(id: FactId, name: String, initialValue: Boolean) extends Fact

sealed case class IntegerFactList(id: FactId, name: String, initialFacts: List[IntegerFact]) extends Fact
sealed case class StringFactList(id: FactId, name: String, initialFacts: List[StringFact]) extends Fact
sealed case class BooleanFactList(id: FactId, name: String, initialFacts: List[BooleanFact]) extends Fact

/**
 * A value type for the ID of a fact
 *
 * @param value The integer value of the ID
 */
case class FactId(value: BigInt) extends AnyVal with Ordered[FactId] {
  override def compare(that: FactId): Int = value compare that.value

  /**
   * Returns a FactId which has it's value incremented by one from the original
   */
  def increment = new FactId(value + 1)

  /**
   * An alias for `increment`
   */
  def inc = increment

  /**
   * Returns true if the FactId is valid, false otherwise
   *
   * A valid fact id is one whose value is greater than or equal to 0
   */
  def isValid = value >= 0
}
