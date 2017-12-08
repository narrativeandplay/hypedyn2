package org.narrativeandplay.hypedyn.core.undo

import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.api.undo.{Undoable, UndoableStream}
import org.narrativeandplay.hypedyn.core.events.UndoEventDispatcher

/**
 * Change that represents a fact having been updated
 *
 * @param notUpdatedFact The not updated version of the fact
 * @param updatedFact The updated version of the fact
 */
sealed case class FactUpdatedChange(notUpdatedFact: Fact, updatedFact: Fact) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): FactUpdatedChange = FactUpdatedChange(updatedFact, notUpdatedFact)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = {
    UndoEventDispatcher.updateFact(notUpdatedFact, updatedFact)
    UndoableStream.send(this)
  }
}
