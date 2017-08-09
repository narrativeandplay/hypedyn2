package org.narrativeandplay.hypedyn.api.story.rules

/**
 * A definition of a parameter for a rule
 *
 * @param name The name of the parameter
 * @param possibleValues The range of values the parameter can take
 */
sealed case class RuleParameter(name: String, possibleValues: ParameterValues)


/**
 * Enumeration representing the types of values a parameter may have
 */
sealed trait ParameterValues
object ParameterValues {
  case object Nodes extends ParameterValues
  case object Links extends ParameterValues

  case object IntegerFacts extends ParameterValues
  case object BooleanFacts extends ParameterValues
  case object StringFacts extends ParameterValues

  case object UserInputString extends ParameterValues
  case object UserInputInteger extends ParameterValues

  /**
   * A list of values to use for the parameter
   *
   * @param values The list of values to use
   */
  sealed case class ListOfValues(values: String*) extends ParameterValues

  /**
   * A mutually exclusive list of potential value types for the parameter
   *
   * @param valueTypes The available value types; the keys of the map is the description used in the UI
   *                   for the value type
   */
  sealed case class Union(valueTypes: Map[String, RuleParameter]) extends ParameterValues

  /**
   * A set of value types that together represent a value for a parameter. Analagous to tuple types.
   *
   * @param valueTypes The set of value types
   */
  sealed case class Product(valueTypes: List[RuleParameter]) extends ParameterValues
}
