package org.narrativeandplay.hypedyn.undo

import org.fxmisc.undo.UndoManagerFactory
import org.reactfx.EventSource

object UndoController {
  private val changes = new EventSource[Undoable]
  private val undoManager = UndoManagerFactory.unlimitedHistoryUndoManager(
    changes,
    { c: Undoable => c.redo() },
    { c: Undoable => c.undo() },
    { (c1: Undoable, c2: Undoable) => c1 mergeWith c2 }
  )

  /**
   * Send a change to the undo controller to allow undo/redo
   *
   * @param change The change to be undoable
   */
  def send(change: Undoable) = changes.push(change)

  /**
   * Undo a change
   */
  def undo(): Unit = undoManager.undo()

  /**
   * Redo a change
   */
  def redo(): Unit = undoManager.redo()
}
