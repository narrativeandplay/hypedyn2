package org.narrativeandplay.hypedyn.story.rules

sealed case class RuleParameter(name: String, possibleValues: ParameterValues)
