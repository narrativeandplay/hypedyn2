package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.rules.internal.Rule
import org.narrativeandplay.hypedyn.story.{NodeId, Nodal}

/**
 * Class representing a node of a story
 *
 * @param id The ID of the node. The ID of a node *must* be unique, no 2 nodes may have the same ID
 * @param name The name of the node
 * @param content The content of the node
 * @param isStartNode Determines whether this node is the starting of the story. In each story, only 1 node is allowed
 *                    to be the start node
 * @param rules The list of rules associated directly with this node
 */
case class Node(id: NodeId,
                name: String,
                content: NodeContent,
                isStartNode: Boolean,
                rules: List[Rule]) extends Nodal {
  override def hashCode(): Int = id.hashCode()

  override def equals(that: Any): Boolean = that match {
    case that: Node => (that canEqual this) && (id == that.id)
    case _ => false
  }

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Node]
}
