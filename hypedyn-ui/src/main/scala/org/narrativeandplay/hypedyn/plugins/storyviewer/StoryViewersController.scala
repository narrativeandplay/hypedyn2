package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.PluginsController

import scalafx.scene.control.Control

object StoryViewersController {
  val StoryViewers = PluginsController.Plugins collect {
    case (name, plugin: StoryViewerLike) => name -> plugin
  }

  val defaultViewer: Control = StoryViewers("Default Story Viewer").asInstanceOf[Control]
}
