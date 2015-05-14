package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeDestroyedChange(destroyedNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = UndoEventDispatcher.createNode(destroyedNode)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.destroyNode(destroyedNode)
}
