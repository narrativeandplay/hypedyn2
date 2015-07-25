package org.narrativeandplay.hypedyn.utils

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.SplitPane

class CollapsibleSplitPane extends SplitPane {
  private val paneItems = ObservableBuffer.empty[Node]

  def add(node: Node): Unit = {
    paneItems += node
    items += node
  }

  def remove(node: Node): Node = {
    paneItems -= node
    items -= node
    node
  }

  def hide(node: Node): Unit = {
    items -= node
  }
  def show(node: Node): Unit = {
    if ((paneItems contains node) && isHidden(node)) {
      val shownItemIndexes = items.toList map { n => paneItems indexOf (n: Node) }
      val itemToShowIndex = paneItems indexOf node

      val insertionIndex = (itemToShowIndex :: shownItemIndexes).sorted indexOf itemToShowIndex

      items.add(insertionIndex, node)
    }
  }

  def isShown(node: Node): Boolean = items contains node
  def isHidden(node: Node): Boolean = !isShown(node)
}
