package org.narrativeandplay.hypedyn.story.rules

sealed trait ParameterValues

case object Nodes extends ParameterValues
case object Links extends ParameterValues

case object IntegerFacts extends ParameterValues
case object BooleanFacts extends ParameterValues
case object StringFacts extends ParameterValues

case object UserInput extends ParameterValues

sealed case class ListOfValues(values: String*) extends ParameterValues

sealed case class Union(valueTypes: List[RuleParameter]) extends ParameterValues
