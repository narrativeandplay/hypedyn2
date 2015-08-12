package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.rules.Fact

/**
 * Change that represents a fact having been destroyed
 *
 * @param destroyedFact The destroyed fact
 */
class FactDestroyedChange(destroyedFact: Fact) extends Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  override def undo(): Unit = UndoEventDispatcher.createFact(destroyedFact)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.destroyFact(destroyedFact)
}
