package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeDestroyedChange(destroyedNode: Node, changedNodes: Map[Node, Node]) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = {
    UndoEventDispatcher.createNode(destroyedNode)
    changedNodes foreach { case (unedited, edited) =>
      UndoEventDispatcher.updateNode(edited, unedited)
    }
  }

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = {
    UndoEventDispatcher.destroyNode(destroyedNode)
    changedNodes foreach { case (unedited, edited) =>
      UndoEventDispatcher.updateNode(unedited, edited)
    }
  }
}
