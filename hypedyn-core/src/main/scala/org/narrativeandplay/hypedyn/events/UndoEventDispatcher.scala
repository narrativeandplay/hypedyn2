package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.story.internal.Node
import org.narrativeandplay.hypedyn.undo._

object UndoEventDispatcher {
  EventBus.UndoResponses foreach { _ => UndoController.undo() }
  EventBus.RedoResponses foreach { _ => UndoController.redo() }

  def createNode(node: Node): Unit = {
    EventBus.send(CreateNode(node, UndoEventSourceIdentity))
  }
  def updateNode(node: Node, updatedNode: Node): Unit = {
    EventBus.send(UpdateNode(node, updatedNode, UndoEventSourceIdentity))
  }
  def destroyNode(node: Node): Unit = {
    EventBus.send(DestroyNode(node, UndoEventSourceIdentity))
  }
}
