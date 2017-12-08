package org.narrativeandplay.hypedyn.storyviewer.utils

import org.narrativeandplay.hypedyn.api.story.Nodal
import org.narrativeandplay.hypedyn.api.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike

/**
 * Implicit classes to extend interfaces with additional helper methods
 */
object ViewerConversions {

  /**
   * Extend the `Nodal` interface to provide helper methods
   */
  implicit class StoryViewerNode(nodal: Nodal) {
    /**
     * Returns the list of "follow link" links of a node
     */
    def links = nodal.content.rulesets flatMap (_.rules) filter (_.isLink)

    /**
      * Returns the list of "show in popup" links of a node
      */
    def showInPopups = nodal.content.rulesets flatMap (_.rules) filter (_.isShowInPopup)

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
     * Returns true if a rule is a "follow link to" link, false otherwise
     */
    def isLink = ruleLike.actions map (_.actionType) contains ActionType("LinkTo")
    /**
      * Returns true if a rule is a "show in popup" link, false otherwise
      */
    def isShowInPopup = ruleLike.actions map (_.actionType) contains ActionType("ShowPopupNode")
  }

}
