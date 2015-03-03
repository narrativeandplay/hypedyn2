package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.storyviewer.utils.UnorderedPair

import scala.collection.mutable.ArrayBuffer

case class LinkGroup(endPoints: UnorderedPair[ViewerNode]) {
  def this(node1: ViewerNode, node2: ViewerNode) = this(UnorderedPair(node1, node2))

  val links = ArrayBuffer.empty[Link]

  def apply(index: Int) = links(index)

  def get(index: Int) = links(index)

  def indexOf(link: Link) = links.indexOf(link)

  def size = links.size

  def insert(name: String, from: ViewerNode, to: ViewerNode) = {
    val link = new Link(from, to, name, this)
    links += link
    link
  }
}
