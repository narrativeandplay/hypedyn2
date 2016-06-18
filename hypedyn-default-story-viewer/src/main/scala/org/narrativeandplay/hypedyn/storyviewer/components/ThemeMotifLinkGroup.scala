package org.narrativeandplay.hypedyn.storyviewer.components

import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair

import scala.collection.mutable.ArrayBuffer

/**
  * Class representing a set of links between a theme and a motif
  *
  * @param themotif The motif that connotes the theme
  * @param thetheme the theme connoted by the motif
  */
class ThemeMotifLinkGroup(val themotif: ViewerMotif, val thetheme: ViewerTheme) {

  private val _links = ArrayBuffer.empty[ThemeMotifLink]

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
  def indexOf(link: ThemeMotifLink) = _links indexOf link

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
  def insert(from: ViewerMotif, to: ViewerTheme) = {
    val link = new ThemeMotifLink(from, to, this)
    _links += link
    link
  }

  /**
    * Remove a link from the group
    *
    * @param link The link to remove
    */
  def remove(link: ThemeMotifLink): Unit = _links -= link

  /**
    * Remove a set of links from the group
    *
    * @param links The links to remove
    */
  def removeAll(links: List[ThemeMotifLink]): Unit = _links --= links

}
