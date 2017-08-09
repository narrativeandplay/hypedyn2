package org.narrativeandplay.hypedyn.story

import scala.language.implicitConversions

import org.narrativeandplay.hypedyn.api.story.Narrative.Metadata
import org.narrativeandplay.hypedyn.api.story.{Narrative, Nodal, NodalContent}
import org.narrativeandplay.hypedyn.api.story.NodalContent.RulesetLike
import org.narrativeandplay.hypedyn.story.UiNodeContent.UiRuleset
import org.narrativeandplay.hypedyn.story.UiStory.UiStoryMetadata
import org.narrativeandplay.hypedyn.api.story.rules.{Actionable, Conditional, RuleLike}

/**
 * Implicit conversions for interfaces to UI classes, to simplify conversions
 */
object InterfaceToUiImplementation {
  implicit def actionable2UiAction(actionable: Actionable): UiAction = new UiAction(actionable.actionType,
                                                                                    actionable.params)
  implicit def conditional2UiCondition(conditional: Conditional): UiCondition = new UiCondition(conditional.conditionType,
                                                                                                conditional.params)

  implicit def actionableList2UiActionList(actionableList: List[Actionable]): List[UiAction] =
    actionableList map actionable2UiAction
  implicit def conditionalList2UiConditionList(conditionalList: List[Conditional]): List[UiCondition] =
    conditionalList map conditional2UiCondition

  implicit def ruleLike2UiRule(ruleLike: RuleLike): UiRule = new UiRule(ruleLike.id,
                                                                        ruleLike.name,
                                                                        ruleLike.stopIfTrue,
                                                                        ruleLike.conditionsOp,
                                                                        ruleLike.conditions,
                                                                        ruleLike.actions)
  implicit def ruleLikeList2UiRuleList(ruleLikes: List[RuleLike]): List[UiRule] = ruleLikes map ruleLike2UiRule

  implicit def rulesetLike2UiRuleset(rulesetLike: RulesetLike): UiNodeContent.UiRuleset =
    new UiRuleset(rulesetLike.id, rulesetLike.name, rulesetLike.indexes, rulesetLike.rules)
  implicit def rulesetLikeList2UiRulesetList(rulesetLikes: List[RulesetLike]): List[UiRuleset] =
    rulesetLikes map rulesetLike2UiRuleset

  implicit def nodalContent2UiNodeContent(nodalContent: NodalContent): UiNodeContent =
    UiNodeContent(nodalContent.text, nodalContent.rulesets)

  implicit def nodal2UiNode(nodal: Nodal): UiNode = new UiNode(nodal.id, nodal.name, nodal.content, nodal.isStartNode, nodal.rules)
  implicit def nodalList2UiNodeList(nodals: List[Nodal]): List[UiNode] = nodals map nodal2UiNode

  implicit def narrativeMetadata2UiStoryMetadata(metadata: Metadata): UiStory.UiStoryMetadata =
    new UiStoryMetadata(
      metadata.title,
      metadata.author,
      metadata.description,
      metadata.comments,
      metadata.readerStyle,
      metadata.isBackButtonDisabled,
      metadata.isRestartButtonDisabled)

  implicit def narrative2UiStory(narrative: Narrative): UiStory = new UiStory(narrative.metadata,
                                                                              narrative.facts,
                                                                              narrative.nodes,
                                                                              narrative.rules)
}
