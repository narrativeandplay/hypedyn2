package org.narrativeandplay.hypedyn.story.rules

/**
 * An interface for a rule
 */
trait RuleLike {
  /**
   * Returns the ID of the rule
   */
  def id: RuleId

  /**
   * Returns the name of the rule
   */
  def name: String

  /**
   * Returns true if the execution of further rules is to be stopped if this rule executes, false otherwise
   */
  def stopIfTrue: Boolean

  /**
   * Returns the boolean operator that is to be used when evaluating a set of conditions
   */
  def conditionsOp: BooleanOperator

  /**
   * Returns the list of conditions of the rule
   */
  def conditions: List[Conditional]

  /**
   * Returns the list of actions of the rule
   */
  def actions: List[Actionable]
}

object RuleLike {

  /**
   * A value type for the name of a parameter
   *
   * @param value The name of the parameter
   */
  case class ParamName(value: String) extends AnyVal

  /**
   * A value type for the value of a parameter
   *
   * @param value The value of the parameter
   */
  case class ParamValue(value: String) extends AnyVal
}

/**
 * A value type for the ID of a rule
 *
 * @param value The integer value of the ID
 */
case class RuleId(value: BigInt) extends AnyVal with Ordered[RuleId] {
  override def compare(that: RuleId): Int = value compare that.value

  /**
   * Returns a FactId which has it's value incremented by one from the original
   */
  def increment = new RuleId(value + 1)

  /**
   * An alias for `increment`
   */
  def inc = increment

  /**
   * Returns a FactId which has it's value decremented by one from the original
   */
  def decrement = new RuleId(value - 1)

  /**
   * An alias for `decrement`
   */
  def dec = decrement

  /**
   * Returns true if the RuleId is valid, false otherwise
   *
   * A valid rule id is one whose value is greater than or equal to 0
   */
  def isValid = value >= 0
}

/**
 * Enumeration representing the types of boolean operators usable when evaluating a set of conditions
 */
sealed trait BooleanOperator
object BooleanOperator {
  case object And extends BooleanOperator
  case object Or extends BooleanOperator
}
