package org.narrativeandplay.hypedyn.plugins.storyviewer.components

import scala.collection.mutable.ArrayBuffer

import org.narrativeandplay.hypedyn.plugins.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.NodalContent.RulesetLike
import org.narrativeandplay.hypedyn.story.rules.RuleLike

case class LinkGroup(endPoints: UnorderedPair[ViewerNode]) {
  def this(node1: ViewerNode, node2: ViewerNode) = this(UnorderedPair(node1, node2))

  private val _links = ArrayBuffer.empty[Link]

  def links = _links.toList

  def apply(index: Int) = get(index)

  def get(index: Int) = _links(index)

  def indexOf(link: Link) = _links.indexOf(link)

  def size = _links.size

  def insert(rule: RuleLike, from: ViewerNode, to: ViewerNode) = {
    val link = new Link(from, to, rule, this)
    _links += link
    link
  }
}
