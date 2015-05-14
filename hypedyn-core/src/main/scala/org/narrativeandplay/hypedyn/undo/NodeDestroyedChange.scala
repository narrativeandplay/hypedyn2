package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.{CreateNode, DestroyNode, EventBus}
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeDestroyedChange(destroyedNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = EventBus.send(CreateNode(destroyedNode, UndoEventSourceIdentity))

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = EventBus.send(DestroyNode(destroyedNode, UndoEventSourceIdentity))
}
