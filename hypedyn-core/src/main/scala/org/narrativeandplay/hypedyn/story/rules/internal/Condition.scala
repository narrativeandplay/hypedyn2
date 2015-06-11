package org.narrativeandplay.hypedyn.story.rules.internal

import org.narrativeandplay.hypedyn.story.rules.Conditional

case class Condition(conditionType: String, params: Map[String, String]) extends Conditional
