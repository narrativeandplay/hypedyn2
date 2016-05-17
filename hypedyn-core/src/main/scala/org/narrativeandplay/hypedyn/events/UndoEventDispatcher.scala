package org.narrativeandplay.hypedyn.events

import scalafx.Includes._
import org.narrativeandplay.hypedyn.story.internal.Node
import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.story.themes.internal.{Motif, Theme}
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
    * Sends an event to create a theme
    *
    * @param theme The theme to create
    */
  def createTheme(theme: Theme): Unit = {
    EventBus.send(CreateTheme(theme, UndoEventSourceIdentity))
  }

  /**
    * Sends an event to update a theme
    *
    * @param theme The theme to update
    * @param updatedTheme The updated version of the same theme
    */
  def updateTheme(theme: Theme, updatedTheme: Theme): Unit = {
    EventBus.send(UpdateTheme(theme, updatedTheme, UndoEventSourceIdentity))
  }

  /**
    * Sends an event to destroy a theme
    *
    * @param theme The theme to destroy
    */
  def destroyTheme(theme: Theme): Unit = {
    EventBus.send(DestroyTheme(theme, UndoEventSourceIdentity))
  }

  /**
    * Sends an event to create a motif
    *
    * @param motif The motif to create
    */
  def createMotif(motif: Motif): Unit = {
    EventBus.send(CreateMotif(motif, UndoEventSourceIdentity))
  }

  /**
    * Sends an event to update a motif
    *
    * @param motif The motif to update
    * @param updatedMotif The updated version of the same motif
    */
  def updateMotif(motif: Motif, updatedMotif: Motif): Unit = {
    EventBus.send(UpdateMotif(motif, updatedMotif, UndoEventSourceIdentity))
  }

  /**
    * Sends an event to destroy a motif
    *
    * @param motif The motif to destroy
    */
  def destroyMotif(motif: Motif): Unit = {
    EventBus.send(DestroyMotif(motif, UndoEventSourceIdentity))
  }}
