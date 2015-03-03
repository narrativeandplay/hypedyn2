package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.PluginsController

object StoryViewersController extends PluginsController{
  type T = StoryViewerLike
  override val PluginClassName = classOf[StoryViewerLike].getCanonicalName
}
