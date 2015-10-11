package org.narrativeandplay.hypedyn.undo

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
