package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.{UndoEventDispatcher, UpdateNode, EventBus}
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeUpdatedChange(notUpdatedNode: Node, updatedNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = UndoEventDispatcher.updateNode(updatedNode, notUpdatedNode)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.updateNode(notUpdatedNode, updatedNode)
}
