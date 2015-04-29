package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.NodeLike

class Node(var name: String, var content: String, val id: Long) extends NodeLike {
  override def toString = s"Node { id: $id }"
}
