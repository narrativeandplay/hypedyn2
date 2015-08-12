package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.clipboard.ClipboardController
import org.narrativeandplay.hypedyn.story.Nodal

/**
 * Event dispatcher for clipboard related events
 *
 * All clipboard related events should invoke this object to send events if needed
 */
object ClipboardEventDispatcher {
  val ClipboardEventSourceIdentity = "Clipboard"

  /**
   * Automatically respond to cut/copy/paste events
   */
  EventBus.CutNodeResponses foreach { evt => ClipboardController.cut(evt.node) }
  EventBus.CopyNodeResponses foreach { evt => ClipboardController.copy(evt.node) }
  EventBus.PasteNodeResponses foreach { evt => ClipboardController.paste[Nodal]() }

  /**
   * Function to execute when needing to send events due to a node being cut
   *
   * @param node The node being cut
   */
  def cutNode(node: Nodal): Unit = {
    EventBus.send(DestroyNode(node, ClipboardEventSourceIdentity))
  }

  /**
   * Function to execute when needing to send events due to a node being pasted
   *
   * @param node The node being pasted
   */
  def pasteNode(node: Nodal): Unit = {
    EventBus.send(CreateNode(node, ClipboardEventSourceIdentity))
  }
}
