package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Node

class NodeImpl(var name: String, var content: String, val id: Long) extends Node {
  override def toString = s"Node { id: $id }"
}
