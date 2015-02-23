package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.scene.control.Control

import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.Node

trait StoryViewerLike {
  this: Control with Plugin =>

  type NodeType <: ViewerNode

  def onNodeCreated(node: Node): Unit
  def onNodeEdited(node: Node): Unit
  def onNodeDeleted(node: Node): Unit
}
