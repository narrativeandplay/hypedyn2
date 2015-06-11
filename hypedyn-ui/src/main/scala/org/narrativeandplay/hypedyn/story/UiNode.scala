package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.RuleLike

case class UiNode(id: NodeId,
                  name: String,
                  content: UiNodeContent,
                  isStartNode: Boolean,
                  rules: List[RuleLike]) extends Nodal
