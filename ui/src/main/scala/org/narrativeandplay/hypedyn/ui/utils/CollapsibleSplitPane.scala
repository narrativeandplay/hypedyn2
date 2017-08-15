package org.narrativeandplay.hypedyn.ui.utils

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.SplitPane

/**
 * A SplitPane where items can be shown/hidden
 */
class CollapsibleSplitPane extends SplitPane {
  private val paneItems = ObservableBuffer.empty[Node]

  /**
   * Add an item to this pane
   *
   * @param node The item to add
   */
  def add(node: Node): Unit = {
    paneItems += node
    items += node
  }

  /**
   * Remove an item from this pane
   *
   * @param node The item to remove
   * @return The removed item
   */
  def remove(node: Node): Node = {
    paneItems -= node
    items -= node
    node
  }

  /**
   * Hide an item in the pane
   *
   * @param node The item to hide
   */
  def hide(node: Node): Unit = {
    items -= node
  }

  /**
   * Show an item in the pane
   *
   * @param node The item to show
   */
  def show(node: Node): Unit = {
    if ((paneItems contains node) && isHidden(node)) {
      val shownItemIndexes = items.toList map { n => paneItems indexOf (n: Node) }
      val itemToShowIndex = paneItems indexOf node

      val insertionIndex = (itemToShowIndex :: shownItemIndexes).sorted indexOf itemToShowIndex

      items.add(insertionIndex, node)
    }
  }

  /**
   * Determines whether a given item is shown in the pane
   *
   * @param node The item to check
   * @return True if the item is shown, false otherwise
   */
  def isShown(node: Node): Boolean = items contains node

  /**
   * Determines whether a given item is hidden in the pane
   *
   * @param node The item to check
   * @return True if the item is hidden, false otherwise
   */
  def isHidden(node: Node): Boolean = !isShown(node)
}
