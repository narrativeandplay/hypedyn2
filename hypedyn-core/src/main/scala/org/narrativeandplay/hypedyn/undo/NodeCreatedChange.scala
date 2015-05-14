package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.{CreateNode, DestroyNode, EventBus}
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeCreatedChange(createdNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = EventBus.send(DestroyNode(createdNode, UndoEventSourceIdentity))

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = EventBus.send(CreateNode(createdNode, UndoEventSourceIdentity))
}
