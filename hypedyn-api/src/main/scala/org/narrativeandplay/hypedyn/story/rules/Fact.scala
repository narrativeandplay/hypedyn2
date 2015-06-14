package org.narrativeandplay.hypedyn.story.rules

sealed trait Fact {
  def id: FactId
  def name: String

  override def hashCode(): Int = id.hashCode()

  override def equals(that: Any): Boolean = that match {
    case that: Fact => (that canEqual this) && (id == that.id)
    case _ => false
  }

  def canEqual(that: Any): Boolean = that.isInstanceOf[Fact]
}

sealed case class IntegerFact(id: FactId, name: String, initalValue: BigInt) extends Fact
sealed case class StringFact(id: FactId, name: String, initialValue: String) extends Fact
sealed case class BooleanFact(id: FactId, name: String, initialValue: Boolean) extends Fact

sealed case class IntegerFactList(id: FactId, name: String, initialFacts: List[IntegerFact]) extends Fact
sealed case class StringFactList(id: FactId, name: String, initialFacts: List[StringFact]) extends Fact
sealed case class BooleanFactList(id: FactId, name: String, initialFacts: List[BooleanFact]) extends Fact

case class FactId(value: BigInt) extends AnyVal with Ordered[FactId] {
  override def compare(that: FactId): Int = value compare that.value

  def increment = new FactId(value + 1)
  def inc = increment

  def isValid = value >= 0
}
