package org.narrativeandplay.hypedyn.plugins.storyviewer

sealed trait LinkNameDisplayType

case object OnLinkAlways extends LinkNameDisplayType

case object OnLinkOnClick extends LinkNameDisplayType

case object AtMouseOnClick extends LinkNameDisplayType
