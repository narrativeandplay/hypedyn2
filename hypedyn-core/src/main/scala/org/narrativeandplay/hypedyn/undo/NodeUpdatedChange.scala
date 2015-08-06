package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeUpdatedChange(notUpdatedNode: Node, updatedNode: Node, changedStartNode: Option[(Node, Node)]) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = {
    UndoEventDispatcher.updateNode(updatedNode, notUpdatedNode)
    changedStartNode foreach { case (unchanged, changed) => UndoEventDispatcher.updateNode(changed, unchanged) }
  }

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = {
    UndoEventDispatcher.updateNode(notUpdatedNode, updatedNode)
    changedStartNode foreach { case (unchanged, changed) => UndoEventDispatcher.updateNode(unchanged, changed) }
  }
}
