package org.narrativeandplay.hypedyn.story.rules

sealed trait Fact {
  def id: FactId
  def name: String
}

sealed trait FactPrimitive extends Fact
sealed case class IntegerFact(id: FactId, name: String, value: BigInt) extends FactPrimitive
sealed case class StringFact(id: FactId, name: String, value: String) extends FactPrimitive
sealed case class BooleanFact(id: FactId, name: String, value: Boolean) extends FactPrimitive

sealed case class HFactList(id: FactId, name: String, facts: FactPrimitive*) extends Fact
sealed case class FactList[T <: FactPrimitive](id: FactId, name: String, facts: T*) extends Fact

case class FactId(value: Long) extends AnyVal
