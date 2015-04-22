package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.plugins.storyviewer.ViewerNode

class NodeMovedChange(val node: ViewerNode,
                      val initialPos: Vector2[Double],
                      val finalPos: Vector2[Double]) extends Change[ViewerNode] {
  override def undo(): Unit = node.relocate(initialPos.x, initialPos.y)

  override def redo(): Unit = node.relocate(finalPos.x, finalPos.y)

  override def merge(other: Change[_]): Option[Change[_]] = other match {
    case c: NodeMovedChange =>
      if (node == c.node) Some(new NodeMovedChange(node, initialPos, c.finalPos)) else None
    case _ => None
  }
}
