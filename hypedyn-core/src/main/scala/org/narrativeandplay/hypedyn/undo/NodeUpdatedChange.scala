package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.{UpdateNode, EventBus}
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeUpdatedChange(notUpdatedNode: Node, updatedNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = EventBus.send(UpdateNode(updatedNode, notUpdatedNode, UndoEventSourceIdentity))

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = EventBus.send(UpdateNode(notUpdatedNode, updatedNode, UndoEventSourceIdentity))
}
