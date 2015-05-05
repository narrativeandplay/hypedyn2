package org.narrativeandplay.hypedyn.story

trait StoryLike {
  type NodeType <: NodeLike

  def title: String

  def author: String

  def description: String

  def nodes: Set[NodeType]
}
