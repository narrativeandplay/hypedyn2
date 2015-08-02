package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.storyviewer.StoryViewerContent

class NodeMovedChange(val nodeContainer: StoryViewerContent,
                      val nodeId: NodeId,
                      val initialPos: Vector2[Double],
                      val finalPos: Vector2[Double]) extends Undoable {
  override def undo(): Unit = {
    nodeContainer.nodes find (_.id == nodeId) foreach (_.relocate(initialPos.x, initialPos.y))
  }

  override def redo(): Unit = {
    nodeContainer.nodes find (_.id == nodeId) foreach (_.relocate(finalPos.x, finalPos.y))
  }

  override def merge(other: Undoable): Option[Undoable] = other match {
    case c: NodeMovedChange =>
      if (nodeId == c.nodeId) Some(new NodeMovedChange(nodeContainer, nodeId, initialPos, c.finalPos)) else None
    case _ => None
  }
}