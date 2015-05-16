package org.narrativeandplay.hypedyn.undo

import org.reactfx.{EventStream, EventSource}

object UndoableStream {
  private val _changes = new EventSource[Undoable]


  val changes: EventStream[Undoable] = _changes

  /**
   * Send a change to the undo controller to allow undo/redo
   *
   * @param change The change to be undoable
   */
  def send(change: Undoable) = _changes.push(change)
}
