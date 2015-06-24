package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.story.internal.Node
import org.narrativeandplay.hypedyn.story.rules.Fact
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

  def createFact(fact: Fact): Unit = {
    EventBus.send(CreateFact(fact, UndoEventSourceIdentity))
  }
  def updateFact(fact: Fact, updatedFact: Fact): Unit = {
    EventBus.send(UpdateFact(fact, updatedFact, UndoEventSourceIdentity))
  }
  def destroyFact(fact: Fact): Unit = {
    EventBus.send(DestroyFact(fact, UndoEventSourceIdentity))
  }
}
