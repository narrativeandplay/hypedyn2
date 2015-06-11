package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.story.rules.Actionable

case class Action(actionType: String, params: Map[String, String]) extends Actionable
