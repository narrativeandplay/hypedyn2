package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.clipboard.ClipboardController
import org.narrativeandplay.hypedyn.story.Nodal

object ClipboardEventDispatcher {
  val ClipboardEventSourceIdentity = "Clipboard"

  EventBus.CutNodeResponses foreach { evt => ClipboardController.cut(evt.node) }
  EventBus.CopyNodeResponses foreach { evt => ClipboardController.copy(evt.node) }
  EventBus.PasteNodeResponses foreach { evt => ClipboardController.paste[Nodal]() }

  def cutNode(node: Nodal): Unit = {
    EventBus.send(DestroyNode(node, ClipboardEventSourceIdentity))
  }

  def pasteNode(node: Nodal): Unit = {
    EventBus.send(CreateNode(node, ClipboardEventSourceIdentity))
  }
}
