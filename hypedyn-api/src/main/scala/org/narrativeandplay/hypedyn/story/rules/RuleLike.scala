package org.narrativeandplay.hypedyn.story.rules

trait RuleLike {
  def id: RuleId
  def name: String
  def conditionsOp: BooleanOperator
  def conditions: List[Conditional]
  def actions: List[Actionable]
}

object RuleLike {
  case class ParamName(value: String) extends AnyVal
  case class ParamValue(value: String) extends AnyVal
}

case class RuleId(value: BigInt) extends AnyVal with Ordered[RuleId] {
  override def compare(that: RuleId): Int = value compare that.value

  def increment = new RuleId(value + 1)
  def inc = increment

  def decrement = new RuleId(value - 1)
  def dec = decrement

  def isValid = value >= 0
}

sealed trait BooleanOperator
case object And extends BooleanOperator
case object Or extends BooleanOperator
