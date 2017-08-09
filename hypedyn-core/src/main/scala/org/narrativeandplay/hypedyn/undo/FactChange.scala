package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.api.undo.Undoable
import org.narrativeandplay.hypedyn.events.UndoEventDispatcher

sealed abstract class FactChange(changedFact: Fact, f: Fact => Unit) extends Undoable {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): FactChange

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = f(changedFact)
}

sealed case class FactCreatedChange(createdFact: Fact)
  extends FactChange(createdFact, UndoEventDispatcher.createFact) {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): FactChange = FactDestroyedChange(createdFact)
}

sealed case class FactDestroyedChange(destroyedFact: Fact)
  extends FactChange(destroyedFact, UndoEventDispatcher.destroyFact) {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): FactChange = FactCreatedChange(destroyedFact)
}
