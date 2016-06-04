package org.narrativeandplay.hypedyn.storyviewer.components

import scala.collection.mutable.ArrayBuffer

import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair

/**
  * Class representing a set of links between 2 nodes
  *
  * @param endPoints The 2 nodes that the links span
  */
class ThematicLinkGroup(val endPoints: UnorderedPair[ViewerNode]) {
  def this(node1: ViewerNode, node2: ViewerNode) = this(UnorderedPair(node1, node2))

  private val _links = ArrayBuffer.empty[ThematicLink]

  /**
    * Returns the list of links that this link group contains
    */
  def links = _links.toList

  /**
    * Get the element at a specific index
    *
    * @param index The index of the element to get
    * @return The element at the given index
    */
  def apply(index: Int) = get(index)

  /**
    * Get the element at a specific index
    *
    * @param index The index of the element to get
    * @return The element at the given index
    */
  def get(index: Int) = _links(index)

  /**
    * Get the index of a given link
    *
    * @param link The link to obtain the index of
    * @return The index of the link, or -1 if the link is not contained within this link group
    */
  def indexOf(link: ThematicLink) = _links indexOf link

  /**
    * Returns the size of the group, i.e. the number of links in the group
    */
  def size = _links.size

  /**
    * Inset a new link into the group
    *
    * @param from The origin of the link
    * @param to The end point of the link
    * @return The created link
    */
  def insert(from: ViewerNode, to: ViewerNode) = {
    val link = new ThematicLink(from, to, this)
    _links += link
    link
  }

  /**
    * Remove a link from the group
    *
    * @param link The link to remove
    */
  def remove(link: ThematicLink): Unit = _links -= link

  /**
    * Remove a set of links from the group
    *
    * @param links The links to remove
    */
  def removeAll(links: List[ThematicLink]): Unit = _links --= links

}
