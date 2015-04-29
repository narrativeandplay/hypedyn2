package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.{NodeLike, StoryLike}

import scala.collection.mutable.ArrayBuffer


class Story(var title: String = "Untitled",
            var author: String = "",
            var description: String = "") extends StoryLike {
  val storyNodes = ArrayBuffer.empty[Node]

  override def nodes: List[NodeLike] = storyNodes.toList
}
