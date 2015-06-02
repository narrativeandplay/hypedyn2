package org.narrativeandplay.hypedyn.story.rules

sealed trait Fact {
  def id: FactId
  def name: String
}

sealed case class IntegerFact(id: FactId, name: String, value: BigInt) extends Fact
sealed case class StringFact(id: FactId, name: String, value: String) extends Fact
sealed case class BooleanFact(id: FactId, name: String, value: Boolean) extends Fact

sealed case class IntegerFactList(id: FactId, name: String, facts: IntegerFact*) extends Fact
sealed case class StringFactList(id: FactId, name: String, facts: StringFact*) extends Fact
sealed case class BooleanFactList(id: FactId, name: String, facts: BooleanFact*) extends Fact

case class FactId(value: Long) extends AnyVal with Ordered[FactId] {
  override def compare(that: FactId): Int = value compare that.value

  def increment = new FactId(value + 1)
  def inc = increment

  def isValid = value >= 0
}
