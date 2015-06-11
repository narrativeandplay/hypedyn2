package org.narrativeandplay.hypedyn.story

import scala.language.implicitConversions

import org.narrativeandplay.hypedyn.story.internal.{Story, NodeContent, Node}
import org.narrativeandplay.hypedyn.story.rules.{Conditional, Actionable, RuleLike}
import org.narrativeandplay.hypedyn.story.rules.internal.{Condition, Action, Rule}

object InterfaceToImplementationConversions {
  implicit def ruleLike2Rule(ruleLike: RuleLike): Rule = Rule(ruleLike.id,
                                                              ruleLike.name,
                                                              ruleLike.conditionsOp,
                                                              ruleLike.conditions map conditional2Condition,
                                                              ruleLike.actions map actionable2Action)
  implicit def actionable2Action(actionable: Actionable): Action = Action(actionable.actionType, actionable.params)
  implicit def conditional2Condition(conditional: Conditional): Condition = Condition(conditional.conditionType,
                                                                                      conditional.params)

  implicit def nodalContent2NodeContent(nodalContent: NodalContent): NodeContent =
    NodeContent(nodalContent.text, nodalContent.rulesets map { case (k, v) => k -> ruleLike2Rule(v) })
  implicit def nodal2Node(nodal: Nodal): Node = Node(nodal.id,
                                                     nodal.name,
                                                     nodalContent2NodeContent(nodal.content),
                                                     nodal.isStartNode,
                                                     nodal.rules map ruleLike2Rule)
  implicit def narrative2Story(narrative: Narrative): Story = Story(narrative.title,
                                                                    narrative.author,
                                                                    narrative.description,
                                                                    narrative.nodes map nodal2Node,
                                                                    narrative.facts,
                                                                    narrative.rules map ruleLike2Rule)

}
