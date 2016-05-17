package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.themes.internal.Motif

sealed abstract class MotifChange(changedMotif: Motif, t: Motif => Unit) extends Undoable {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): MotifChange

  /**
    * Defines how to reverse an undo action
    */
  override def redo(): Unit = t(changedMotif)
}

sealed case class MotifCreatedChange(createdMotif: Motif)
  extends MotifChange(createdMotif, UndoEventDispatcher.createMotif) {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): MotifChange = MotifDestroyedChange(createdMotif)
}

sealed case class MotifDestroyedChange(destroyedMotif: Motif)
  extends MotifChange(destroyedMotif, UndoEventDispatcher.destroyMotif) {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): MotifChange = MotifCreatedChange(destroyedMotif)
}