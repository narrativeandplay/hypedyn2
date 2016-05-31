package org.narrativeandplay.hypedyn.story

import scala.language.implicitConversions
import org.narrativeandplay.hypedyn.story.internal.Story.Metadata
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.rules.{Actionable, Conditional, RuleLike}
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, Condition, Rule}
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThemeLike}
import org.narrativeandplay.hypedyn.story.themes.internal.{Motif, Theme}

/**
 * Implicit conversions for converting interface types into concrete types used by
 * the core. Used for simplifying working with interface types
 */
object InterfaceToImplementationConversions {
  implicit def ruleLike2Rule(ruleLike: RuleLike): Rule = Rule(ruleLike.id,
                                                              ruleLike.name,
                                                              ruleLike.stopIfTrue,
                                                              ruleLike.conditionsOp,
                                                              ruleLike.conditions map conditional2Condition,
                                                              ruleLike.actions map actionable2Action)
  implicit def actionable2Action(actionable: Actionable): Action = Action(actionable.actionType, actionable.params)
  implicit def actionableList2ActionList(actionableList: List[Actionable]): List[Action] =
    actionableList map actionable2Action
  implicit def conditional2Condition(conditional: Conditional): Condition = Condition(conditional.conditionType,
                                                                                      conditional.params)
  implicit def conditionalList2ConditionList(conditionalList: List[Conditional]): List[Condition] =
    conditionalList map conditional2Condition

  implicit def rulesetLike2Ruleset(rulesetLike: NodalContent.RulesetLike): NodeContent.Ruleset =
    NodeContent.Ruleset(rulesetLike.id, rulesetLike.name, rulesetLike.indexes, rulesetLike.rules map ruleLike2Rule)

  implicit def themeLike2Theme(themeLike: ThemeLike): Theme = Theme(themeLike.id,
    themeLike.name,
    themeLike.subthemes,
    themeLike.motifs)
  implicit def motifLike2Motif(motifLike: MotifLike): Motif = Motif(motifLike.id,
    motifLike.name,
    motifLike.features)
  implicit def nodalContent2NodeContent(nodalContent: NodalContent): NodeContent =
    NodeContent(nodalContent.text, nodalContent.rulesets map rulesetLike2Ruleset)
  implicit def nodal2Node(nodal: Nodal): Node = Node(nodal.id,
                                                     nodal.name,
                                                     nodalContent2NodeContent(nodal.content),
                                                     nodal.isStartNode,
                                                     nodal.rules map ruleLike2Rule)
  implicit def narrativeMetadata2StoryMetadata(metadata: Narrative.Metadata): Story.Metadata =
    new Metadata(metadata.comments, metadata.readerStyle, metadata.isBackButtonDisabled, metadata.isRestartButtonDisabled, metadata.themeThreshold)
  implicit def narrative2Story(narrative: Narrative): Story = Story(narrative.title,
                                                                    narrative.author,
                                                                    narrative.description,
                                                                    narrative.metadata,
                                                                    narrative.nodes map nodal2Node,
                                                                    narrative.facts,
                                                                    narrative.rules map ruleLike2Rule,
                                                                    narrative.themes map themeLike2Theme,
                                                                    narrative.motifs map motifLike2Motif)

}
