package org.narrativeandplay.hypedyn

import scalafx.scene.input.{ClipboardContent, Clipboard, DataFormat}

import org.narrativeandplay.hypedyn.events.ClipboardEventDispatcher
import org.narrativeandplay.hypedyn.story.rules.RuleId
import org.narrativeandplay.hypedyn.story.rules.internal.Rule
import org.narrativeandplay.hypedyn.story.{NodalContent, Nodal, NodeId}
import org.narrativeandplay.hypedyn.story.internal.{NodeContent, Node}
import org.narrativeandplay.hypedyn.story.InterfaceToImplementationConversions._

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

      val nodeContent = NodeContent(t.content.text, t.content.rulesets map { ruleset =>
        ruleset.copy(id = NodalContent.RulesetId(-1),
                     rules = ruleset.rules map (_.copy(id = RuleId(-1))))
      })
      val nodeRules = t.rules map { rule =>
        Rule(RuleId(-1), rule.name, rule.conditionsOp, rule.conditions, rule.actions)
      }
      val node = Node(NodeId(-1), t.name, nodeContent, isStartNode = false, nodeRules)
      clipboardContent.put(NodeDataFormat, node)
      clipboardContent.putString(nodeContent.text)

      clipboard.setContent(clipboardContent)
    }
  }

}
