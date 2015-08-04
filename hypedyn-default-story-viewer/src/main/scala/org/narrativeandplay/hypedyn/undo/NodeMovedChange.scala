package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer

class NodeMovedChange(val eventHandler: StoryViewer,
                      val nodeId: NodeId,
                      val initialPos: Vector2[Double],
                      val finalPos: Vector2[Double]) extends Undoable {
  override def undo(): Unit = eventHandler.moveNode(nodeId, initialPos)

  override def redo(): Unit = eventHandler.moveNode(nodeId, finalPos)

  override def merge(other: Undoable): Option[Undoable] = other match {
    case c: NodeMovedChange =>
      if (nodeId == c.nodeId) Some(new NodeMovedChange(eventHandler, nodeId, initialPos, c.finalPos)) else None
    case _ => None
  }
}