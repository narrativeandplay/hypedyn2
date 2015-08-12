package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

/**
 * Change that represents a node having been created
 *
 * @param createdNode The created node
 */
class NodeCreatedChange(createdNode: Node) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = UndoEventDispatcher.destroyNode(createdNode)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.createNode(createdNode)
}
