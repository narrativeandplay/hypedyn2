package org.narrativeandplay.hypedyn.storyviewer.components

import scala.collection.mutable.ArrayBuffer

import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.rules.RuleLike

class LinkGroup(val endPoints: UnorderedPair[ViewerNode]) {
  def this(node1: ViewerNode, node2: ViewerNode) = this(UnorderedPair(node1, node2))

  private val _links = ArrayBuffer.empty[Link]

  def links = _links.toList

  def apply(index: Int) = get(index)

  def get(index: Int) = _links(index)

  def indexOf(link: Link) = _links indexOf link

  def size = _links.size

  def insert(from: ViewerNode, to: ViewerNode, rule: RuleLike) = {
    val link = new Link(from, to, rule, this)
    _links += link
    link
  }

  def remove(link: Link): Unit = _links -= link
  def removeAll(links: List[Link]): Unit = _links --= links

}
