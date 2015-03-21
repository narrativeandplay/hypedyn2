package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.PluginsController

import scalafx.scene.control.Control

object StoryViewersController extends PluginsController{
  type T = StoryViewerLike
  override val PluginClassName = classOf[StoryViewerLike].getCanonicalName
  
  val defaultViewer: Control = Plugins("Default Story Viewer").asInstanceOf[Control]
}
