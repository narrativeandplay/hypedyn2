package org.narrativeandplay.hypedyn.storyviewer.utils

import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike

/**
 * Implicit classes to extend interfaces with additional helper methods
 */
object ViewerConversions {

  /**
   * Extend the `Nodal` interface to provide helper methods
   */
  implicit class StoryViewerNode(nodal: Nodal) {
    /**
     * Returns the list of links of a node
     */
    def links = nodal.content.rulesets flatMap (_.rules) filter (_.isLink)

    /**
     * Returns true is the node is an anywhere node, false otherwise
     */
    def isAnywhere = {
      val actionTypes = nodal.rules flatMap (_.actions) map (_.actionType)

      (actionTypes contains ActionType("EnableAnywhereLinkToHere")) ||
        (actionTypes contains ActionType("ShowDisabledAnywhereLink"))
    }
  }

  /**
   * Extends the `RuleLike` interface to provide helper methods
   */
  implicit class StoryViewerRule(ruleLike: RuleLike) {
    /**
     * Returns true if a rule is a link, false otherwise
     */
    def isLink = ruleLike.actions map (_.actionType) contains ActionType("LinkTo")
  }

}
