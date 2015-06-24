package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.rules.Fact

class FactUpdatedChange(notUpdatedFact: Fact, updatedFact: Fact) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = UndoEventDispatcher.updateFact(updatedFact, notUpdatedFact)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.updateFact(notUpdatedFact, updatedFact)
}
