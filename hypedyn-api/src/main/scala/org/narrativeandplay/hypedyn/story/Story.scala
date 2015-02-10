package org.narrativeandplay.hypedyn.story

import scala.collection.mutable.ArrayBuffer

sealed class Story(var title: String = "Untitled",
                   var author: String = "",
                   var description: String = "") {
  private val storyNodes = ArrayBuffer.empty[Node]

  def nodes = storyNodes.toList
}
