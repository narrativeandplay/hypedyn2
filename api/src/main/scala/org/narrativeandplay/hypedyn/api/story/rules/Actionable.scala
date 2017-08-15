package org.narrativeandplay.hypedyn.api.story.rules

/**
 * Types of actions
 *
 * Text-attached rules
 * - Link to
 * - show node in popup
 * - Update fact
 *  - Integer
 *    - to given value
 *    - with other integer fact's value
 *    - with computation (b/w facts and/or input values)
 *    - random in range
 *  - Boolean
 *    - to given value
 *  - String
 *    - to given value
 * - Update text
 *
 * Node attached rules
 * - update fact
 *  - as per text-attached rules
 * - enable link to this node from anywhere
 * - show disabled anywhere link
 */
/**
 * An interface representing an instance of an Action
 */
trait Actionable {
  /**
   * Returns the type of action being instanced
   */
  def actionType: Actionable.ActionType

  /**
   * Returns the parameters of the instanced action and their values
   */
  def params: Map[RuleLike.ParamName, RuleLike.ParamValue]
}

object Actionable {

  /**
   * A value type that represents the type of an Action
   *
   * @param value The type of the action
   */
  case class ActionType(value: String) extends AnyVal
}

/**
 * A class that defines a new action type
 *
 * @param actionType The name of the action type being defined
 * @param description The description of the action, also used as its string representation in the UI
 * @param actionLocationTypes The locations the action may be used
 * @param canActivate Whether this action makes the associated fragment clickable
 * @param parameters The list of parameters that the action has
 */
sealed case class ActionDefinition(actionType: Actionable.ActionType,
                                   description: String,
                                   actionLocationTypes: List[ActionLocationType],
                                   canActivate: Boolean,
                                   parameters: List[RuleParameter])

/**
 * Enumeration representing the locations where an action may be used
 */
sealed trait ActionLocationType
object ActionLocationType {
  case object NodeAction extends ActionLocationType
  case object NodeContentAction extends ActionLocationType
  case object StoryAction extends ActionLocationType
}
