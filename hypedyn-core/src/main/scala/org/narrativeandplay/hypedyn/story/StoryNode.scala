package org.narrativeandplay.hypedyn.story

class StoryNode(var name: String, var content: String, val id: Long) extends Node {
  def this(name: String, content: String) = this(name, content, StoryNode.firstUnusedId)

  StoryNode.firstUnusedId = math.max(StoryNode.firstUnusedId + 1, id + 1)
}

object StoryNode {
  private var firstUnusedId = 0.toLong
}
