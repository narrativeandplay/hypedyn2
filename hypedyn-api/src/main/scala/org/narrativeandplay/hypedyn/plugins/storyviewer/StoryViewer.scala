package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.events.{NodeDeletedEvent, NodeEditedEvent, NodeCreatedEvent, EventBus}
import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.Node

trait StoryViewer {
  this: Plugin =>

  type NodeType <: ViewerNode

  EventBus.nodeCreatedEvents.subscribe((evt: NodeCreatedEvent) => {
    onNodeCreated(evt.node)
  })

  EventBus.nodeEditedEvents.subscribe((evt: NodeEditedEvent) => {
    onNodeEdited(evt.node)
  })

  EventBus.nodeDeletedEvents.subscribe((evt: NodeDeletedEvent) => {
    onNodeDeleted(evt.node)
  })

  def onNodeCreated(node: Node): Unit
  def onNodeEdited(node: Node): Unit
  def onNodeDeleted(node: Node): Unit
}
