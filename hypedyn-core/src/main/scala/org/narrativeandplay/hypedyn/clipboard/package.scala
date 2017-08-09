package org.narrativeandplay.hypedyn

import scalafx.scene.input.{Clipboard, ClipboardContent, DataFormat}

import org.narrativeandplay.hypedyn.api.clipboard.Copyable
import org.narrativeandplay.hypedyn.api.story.rules.RuleId
import org.narrativeandplay.hypedyn.api.story.{Nodal, NodalContent, NodeId}
import org.narrativeandplay.hypedyn.events.ClipboardEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent}
import org.narrativeandplay.hypedyn.story.InterfaceToImplementationConversions._

package object clipboard {

  /**
   * Typeclass instance for cutting/copying/pasting nodes
   */
  implicit object NodeCopier extends Copyable[Nodal] {
    val NodeDataFormat = new DataFormat("application/x-hypedyn2-node")
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

      val nodeContent = NodeContent(t.content.text, t.content.rulesets map { ruleset =>
        ruleset.copy(id = NodalContent.RulesetId(-1),
                     rules = ruleset.rules map (_.copy(id = RuleId(-1))))
      })
      val nodeRules = t.rules map { rule =>
        rule.copy(id = RuleId(-1))
      }
      val node = Node(NodeId(-1), t.name, nodeContent, isStartNode = false, nodeRules)
      clipboardContent.put(NodeDataFormat, node)
      clipboardContent.putString(nodeContent.text)

      clipboard.setContent(clipboardContent)
    }
  }

}
