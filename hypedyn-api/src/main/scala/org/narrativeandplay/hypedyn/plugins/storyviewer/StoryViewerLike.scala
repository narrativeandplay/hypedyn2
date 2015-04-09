package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.events.EventBus
import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.Node

import scalafx.scene.control.Control

trait StoryViewerLike {
  this: Control with Plugin =>

  EventBus.nodeCreatedEvents subscribe { evt => onNodeCreated(evt.node) }
  EventBus.nodeUpdatedEvents subscribe { evt => onNodeUpdated(evt.node) }
  EventBus.nodeDestroyedEvents subscribe { evt => onNodeDestroyed(evt.node) }

  def onNodeCreated(node: Node): Unit
  def onNodeUpdated(node: Node): Unit
  def onNodeDestroyed(node: Node): Unit
}
