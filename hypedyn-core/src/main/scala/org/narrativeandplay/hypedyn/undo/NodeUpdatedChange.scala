package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

/**
 * Change that represents a node having been updated
 *
 * @param notUpdatedNode The not updated version of the node
 * @param updatedNode The updated version of the node
 * @param changedStartNode An option containing the unchanged and changed versions of the start node, or None if no
 *                         node was changed when the node was set as the start node or if this node was already the
 *                         start node
 */
sealed case class NodeUpdatedChange(notUpdatedNode: Node, updatedNode: Node, changedStartNode: Option[(Node, Node)]) extends Undoable {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): NodeUpdatedChange = NodeUpdatedChange(updatedNode, notUpdatedNode, changedStartNode map (_.swap))

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = {
    UndoEventDispatcher.updateNode(notUpdatedNode, updatedNode)
    changedStartNode foreach { case (unchanged, changed) => UndoEventDispatcher.updateNode(unchanged, changed) }
  }
}
