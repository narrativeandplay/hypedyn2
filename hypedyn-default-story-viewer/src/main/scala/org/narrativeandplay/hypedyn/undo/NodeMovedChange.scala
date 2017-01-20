package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer

/**
 * Change representing a node having been moved
 *
 * @param eventHandler The event dispatcher allowed to send events
 * @param nodeId The ID of the node moved
 * @param initialPos The initial position of the node
 * @param finalPos The final position of the node
 */
case class NodeMovedChange(eventHandler: StoryViewer,
                           nodeId: NodeId,
                           initialPos: Vector2[Double],
                           finalPos: Vector2[Double]) extends Undoable {
  override def undo(): NodeMovedChange = NodeMovedChange(eventHandler, nodeId, finalPos, initialPos)

  override def redo(): Unit = {
    eventHandler.moveNode(nodeId, finalPos)
    eventHandler.notifyNodeMove(nodeId, initialPos, finalPos)
  }

  override def merge(other: Undoable): Option[Undoable] = other match {
    case c: NodeMovedChange =>
      if (nodeId == c.nodeId) Some(NodeMovedChange(eventHandler, nodeId, initialPos, c.finalPos)) else None
    case _ => None
  }
}
