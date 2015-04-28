package org.narrativeandplay.hypedyn.clipboard

import org.narrativeandplay.hypedyn.events.{CreateNode, DeleteNode, EventBus}
import org.narrativeandplay.hypedyn.story.Node

import scalafx.scene.input.{ClipboardContent, Clipboard}

object ClipboardController {
  private val clipboard = new Clipboard(Clipboard.systemClipboard)

  def cut(node: Node): Unit = {
    copy(node)

    EventBus send DeleteNode(node)
  }

  def copy(node: Node): Unit = {
    val c = new ClipboardContent()

    val n = new Node {
      override def name: String = node.name

      override def content: String = node.content

      override def id: Long = -1
    }

    c.put(ClipboardDataFormats.nodeFormat, n)

    clipboard.setContent(c)
  }

  def paste(): Unit = {
    val node = clipboard.content(ClipboardDataFormats.nodeFormat).asInstanceOf[Node]

    EventBus.send(CreateNode(node))
  }
}
