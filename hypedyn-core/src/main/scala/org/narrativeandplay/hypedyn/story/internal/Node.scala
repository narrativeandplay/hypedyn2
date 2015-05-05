package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.serialisation.{SaveString, SaveInt, SaveHash}
import org.narrativeandplay.hypedyn.story.NodeLike

class Node(val name: String, val content: String, val id: Long) extends NodeLike {
  override def toString = s"Node { id: $id }"

  def serialise = SaveHash("id" -> SaveInt(id),
                           "content" -> SaveString(content),
                           "name" -> SaveString(name))
}

object Node {
  def deserialise(nodeData: SaveHash) = {
    val name = nodeData("name").asInstanceOf[SaveString].s
    val content = nodeData("content").asInstanceOf[SaveString].s
    val id = nodeData("id").asInstanceOf[SaveInt].i

    new Node(name, content, id)
  }
}
