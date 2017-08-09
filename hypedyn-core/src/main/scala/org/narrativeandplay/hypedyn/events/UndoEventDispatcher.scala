package org.narrativeandplay.hypedyn.events

import scalafx.Includes._

import org.narrativeandplay.hypedyn.api.events._
import org.narrativeandplay.hypedyn.api.story.Narrative
import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.story.internal.Node
import org.narrativeandplay.hypedyn.undo._

/**
 * Event dispatcher for handling undo/redo
 *
 * Any undo action that needs to invoke events should do it through this dispatcher
 */
object UndoEventDispatcher {
  /**
   * Automatically respond to undo/redo actions
   */
  EventBus.UndoResponses foreach { _ => UndoController.undo() }
  EventBus.RedoResponses foreach { _ => UndoController.redo() }

  EventBus.send(UndoStatus(UndoController.undoAvailable(), UndoEventSourceIdentity))
  EventBus.send(RedoStatus(UndoController.redoAvailable(), UndoEventSourceIdentity))
  UndoController.markCurrentPosition()

  UndoController.undoAvailable onChange { (_, _, available) =>
    EventBus.send(UndoStatus(available, UndoEventSourceIdentity))
  }
  UndoController.redoAvailable onChange { (_, _, available) =>
    EventBus.send(RedoStatus(available, UndoEventSourceIdentity))
  }

  UndoController.atMarkedPosition onChange { (_, _, isAtMarkedPos) =>
    EventBus.send(FileStatus(!isAtMarkedPos, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to create a node
   *
   * @param node The node to create
   */
  def createNode(node: Node): Unit = {
    EventBus.send(CreateNode(node, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to update a node
   *
   * @param node The node to update
   * @param updatedNode The updated version of the same node
   */
  def updateNode(node: Node, updatedNode: Node): Unit = {
    EventBus.send(UpdateNode(node, updatedNode, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to destroy a node
   *
   * @param node The node to destroy
   */
  def destroyNode(node: Node): Unit = {
    EventBus.send(DestroyNode(node, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to create a fact
   *
   * @param fact The fact to create
   */
  def createFact(fact: Fact): Unit = {
    EventBus.send(CreateFact(fact, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to update a fact
   *
   * @param fact The fact to update
   * @param updatedFact The updated version of the same fact
   */
  def updateFact(fact: Fact, updatedFact: Fact): Unit = {
    EventBus.send(UpdateFact(fact, updatedFact, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to destroy a fact
   *
   * @param fact The fact to destroy
   */
  def destroyFact(fact: Fact): Unit = {
    EventBus.send(DestroyFact(fact, UndoEventSourceIdentity))
  }

  /**
   * Sends an event to update the story properties
   *
   * @param newStoryProperties The updated story properties
   */
  def updateStoryProperties(newStoryProperties: Narrative.Metadata): Unit = {
    EventBus.send(UpdateStoryProperties(newStoryProperties, UndoEventSourceIdentity))
  }
}
