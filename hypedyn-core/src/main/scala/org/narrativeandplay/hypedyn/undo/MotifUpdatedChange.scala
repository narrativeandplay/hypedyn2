package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.themes.internal.Motif

/**
  * Change that represents a motif having been updated
  *
  * @param notUpdatedMotif The not updated version of the motif
  * @param updatedMotif The updated version of the motif
  */
sealed case class MotifUpdatedChange(notUpdatedMotif: Motif, updatedMotif: Motif) extends Undoable {
  /**
    * Defines what to do when an undo action happens
    */
  override def undo(): MotifUpdatedChange = MotifUpdatedChange(updatedMotif, notUpdatedMotif)

  /**
    * Defines how to reverse an undo action
    */
  override def redo(): Unit = UndoEventDispatcher.updateMotif(notUpdatedMotif, updatedMotif)
}
