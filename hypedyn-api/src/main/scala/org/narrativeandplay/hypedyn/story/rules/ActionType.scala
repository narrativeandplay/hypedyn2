package org.narrativeandplay.hypedyn.story.rules

sealed trait ActionType

case object NodeAction extends ActionType
case object NodeContentAction extends ActionType
case object StoryAction extends ActionType