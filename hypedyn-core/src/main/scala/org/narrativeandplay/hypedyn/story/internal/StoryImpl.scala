package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.{Node, Story}

import scala.collection.mutable.ArrayBuffer


class StoryImpl(var title: String = "Untitled",
                var author: String = "",
                var description: String = "") extends Story {
  val storyNodes = ArrayBuffer.empty[NodeImpl]

  override def nodes: List[Node] = storyNodes.toList
}
