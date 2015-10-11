package org.narrativeandplay.hypedyn.undo

import javafx.beans.value.ObservableBooleanValue

import org.fxmisc.undo.UndoManagerFactory

/**
 * Controller handling undo events
 */
object UndoController {
  private val undoManager = UndoManagerFactory.unlimitedHistoryUndoManager(
    UndoableStream.changes,
    { c: Undoable => c.redo() },
    { c: Undoable => c.undo() },
    { (c1: Undoable, c2: Undoable) => c1 mergeWith c2 }
  )

  /**
   * Observable stream of whether the current position within the undo manager's
   * history is the same as the last marked position.
   */
  val atMarkedPosition: ObservableBooleanValue = undoManager.atMarkedPositionProperty()

  /**
   * Undo a change
   */
  def undo(): Unit = undoManager.undo()

  /**
   * Redo a change
   */
  def redo(): Unit = undoManager.redo()

  /**
   * Clears the undo history
   */
  def clearHistory(): Unit = {
    undoManager.forgetHistory()
  }

  /**
   * Marks the current position in the undo queue
   */
  def markCurrentPosition(): Unit = undoManager.mark()
}
