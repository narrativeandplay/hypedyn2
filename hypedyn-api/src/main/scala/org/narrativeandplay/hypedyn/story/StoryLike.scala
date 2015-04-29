package org.narrativeandplay.hypedyn.story

trait StoryLike {
  def title: String

  def author: String

  def description: String

  def nodes: List[NodeLike]
}
