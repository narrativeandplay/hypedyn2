package org.narrativeandplay.hypedyn.story.rules

sealed case class RuleParameter(name: String, possibleValues: ParameterValues)


sealed trait ParameterValues

case object Nodes extends ParameterValues
case object Links extends ParameterValues

case object IntegerFacts extends ParameterValues
case object BooleanFacts extends ParameterValues
case object StringFacts extends ParameterValues

case object UserInputString extends ParameterValues
case object UserInputInteger extends ParameterValues

sealed case class ListOfValues(values: String*) extends ParameterValues

sealed case class Union(valueTypes: Map[String, RuleParameter]) extends ParameterValues
sealed case class Product(valueTypes: List[RuleParameter]) extends ParameterValues
