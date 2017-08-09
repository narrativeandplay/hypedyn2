package org.narrativeandplay.hypedyn.api.undo

import org.reactfx.{EventStream, EventSource}

/**
 * Stream of undoable changes
 *
 * This is the source of all changes to the undo manager
 */
object UndoableStream {
  private val _changes = new EventSource[Undoable]

  /**
   * The changes of the stream
   */
  val changes: EventStream[Undoable] = _changes

  /**
   * Send a change to the undo controller to allow undo/redo
   *
   * @param change The change to be undoable
   */
  def send(change: Undoable) = _changes.push(change)
}
