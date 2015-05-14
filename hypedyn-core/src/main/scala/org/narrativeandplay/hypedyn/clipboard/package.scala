package org.narrativeandplay.hypedyn

import scalafx.scene.input.{ClipboardContent, Clipboard, DataFormat}

import org.narrativeandplay.hypedyn.events.ClipboardEventDispatcher
import org.narrativeandplay.hypedyn.story.{Nodal, NodeId}
import org.narrativeandplay.hypedyn.story.internal.Node

package object clipboard {
  implicit object NodeCopier extends Copyable[Nodal] {
    val NodeDataFormat = new DataFormat("application/x-hypedyn-node")
    val clipboard = new Clipboard(Clipboard.systemClipboard)
    /**
     * Defines what to do on a cut action
     */
    override def cut(t: Nodal): Unit = {
      copy(t)

      ClipboardEventDispatcher.cutNode(t)
    }

    /**
     * Defines what to do on a paste action
     */
    override def paste(): Unit = {
      val node = Option(clipboard.content(NodeDataFormat).asInstanceOf[Node])

      node foreach { n => ClipboardEventDispatcher.pasteNode(n) }
    }

    /**
     * Defines what to do on a copy action
     */
    override def copy(t: Nodal): Unit = {
      val clipboardContent = new ClipboardContent()
      val node = new Node(NodeId(-1), t.name, t.content, false)
      clipboardContent.put(NodeDataFormat, node)

      clipboard.setContent(clipboardContent)
    }
  }

}
