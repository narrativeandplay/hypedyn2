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
  def conditionType: String

  /**
   * A mapping of the condition's parameter names to the instanced values
   */
  def params: Map[String, String]
}

/**
 * A class representing a definition of a condition
 *
 * @param conditionName The condition type; this must be unique, and can be thought of as a class name for the condition
 * @param description The text to show in the UI when referencing this condition
 * @param parameters The list of parameters for this condition and their possible values
 */
sealed case class ConditionDefinition(conditionName: String, description: String, parameters: List[RuleParameter])
