package org.narrativeandplay.hypedyn.story.rules

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
trait Actionable {
  def actionType: Actionable.ActionType
  def params: Map[RuleLike.ParamName, RuleLike.ParamValue]
}

object Actionable {
  case class ActionType(value: String) extends AnyVal
}

sealed case class ActionDefinition(actionType: Actionable.ActionType,
                                   description: String,
                                   actionLocationTypes: List[ActionLocationType],
                                   parameters: List[RuleParameter])

sealed trait ActionLocationType
case object NodeAction extends ActionLocationType
case object NodeContentAction extends ActionLocationType
case object StoryAction extends ActionLocationType
