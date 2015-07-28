package org.narrativeandplay.hypedyn.story.rules

/**
 * Types of conditions
 *
 * - Node
 *  - visited
 *  - not visited
 *  - is previous
 *  - is not previous
 * - Link
 *  - followed
 *  - not followed
 * - Boolean fact value
 * - Integer fact value
 *  - comparison with given value
 *  - comparison with another integer fact
 */
/**
 * Trait representing an instance of a condition
 */
trait Conditional {
  /**
   * The type of the condition that is being instanced
   */
  def conditionType: Conditional.ConditionType

  /**
   * A mapping of the condition's parameter names to the instanced values
   */
  def params: Map[RuleLike.ParamName, RuleLike.ParamValue]
}

object Conditional {
  case class ConditionType(value: String) extends AnyVal
}

/**
 * A class representing a definition of a condition
 *
 * @param conditionType The condition type; this must be unique, and can be thought of as a class name for the condition
 * @param description The text to show in the UI when referencing this condition
 * @param parameters The list of parameters for this condition and their possible values
 */
sealed case class ConditionDefinition(conditionType: Conditional.ConditionType,
                                      description: String,
                                      parameters: List[RuleParameter])
