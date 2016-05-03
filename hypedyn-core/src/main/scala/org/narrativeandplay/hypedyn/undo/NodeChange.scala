package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Node

sealed abstract class NodeChange(changedNode: Node, affectedNodes: Map[Node, Node], f: Node => Unit) extends Undoable {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): NodeChange

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = {
    f(changedNode)
    affectedNodes foreach { case (unchanged, changed) =>
      UndoEventDispatcher.updateNode(unchanged, changed)
    }
    UndoableStream.send(this)
  }
}

/**
 * Change that represents a node having been created
 *
 * @param createdNode The created node
 * @param affectedNodes The map of nodes that were changed as a result of the node being created
 */
sealed case class NodeCreatedChange(createdNode: Node, affectedNodes: Map[Node, Node])
  extends NodeChange(createdNode, affectedNodes, UndoEventDispatcher.createNode) {

  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): NodeChange = NodeDestroyedChange(createdNode, affectedNodes map (_.swap))
}

/**
 * Change that represents a node having been destroyed
 *
 * @param destroyedNode The destroyed node
 * @param affectedNodes The map of nodes that were changed as a result of the node being destroyed
 */
sealed case class NodeDestroyedChange(destroyedNode: Node, affectedNodes: Map[Node, Node])
  extends NodeChange(destroyedNode, affectedNodes, UndoEventDispatcher.destroyNode) {

  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): NodeChange = NodeCreatedChange(destroyedNode, affectedNodes map (_.swap))
}
