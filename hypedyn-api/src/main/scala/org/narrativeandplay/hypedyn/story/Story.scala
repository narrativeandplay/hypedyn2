package org.narrativeandplay.hypedyn.story

trait Story {
  def title: String
  def author: String
  def description: String

  def nodes: List[Node]
}
