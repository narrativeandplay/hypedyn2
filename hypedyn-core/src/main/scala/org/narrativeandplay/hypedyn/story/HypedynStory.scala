package org.narrativeandplay.hypedyn.story

import scala.collection.mutable.ArrayBuffer

class HypedynStory(var title: String = "Untitled",
                   var author: String = "",
                   var description: String = "") extends Story {
  val storyNodes = ArrayBuffer.empty[StoryNode]

  def nodes = storyNodes.toList
}