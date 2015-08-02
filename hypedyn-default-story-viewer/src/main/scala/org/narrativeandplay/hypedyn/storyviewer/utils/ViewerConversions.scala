package org.narrativeandplay.hypedyn.storyviewer.utils

import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike

object ViewerConversions {
  implicit class StoryViewerNode(nodal: Nodal) {
    def links = nodal.content.rulesets flatMap (_.rules) filter (_.isLink)
    def isAnywhere = {
      val actionTypes = nodal.rules flatMap (_.actions) map (_.actionType)

      (actionTypes contains ActionType("EnableAnywhereLinkToHere")) ||
        (actionTypes contains ActionType("ShowDisabledAnywhereLink"))
    }
  }

  implicit class StoryViewerRule(ruleLike: RuleLike) {
    def isLink = ruleLike.actions map (_.actionType) contains ActionType("LinkTo")
  }

}
