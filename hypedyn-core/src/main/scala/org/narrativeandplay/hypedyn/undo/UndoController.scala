package org.narrativeandplay.hypedyn.undo

import java.lang
import java.util.function.{Function => JFunction}
import javafx.beans.value.ObservableValue

import scalafx.beans.property.ObjectProperty

import org.fxmisc.easybind.EasyBind
import org.fxmisc.undo.{UndoManager, UndoManagerFactory}
/**
 * Controller handling undo events
 */
object UndoController {
  private val undoManager = ObjectProperty(UndoManagerFactory.unlimitedHistoryUndoManager(
    UndoableStream.changes,
    { c: Undoable => c.redo() },
    { c: Undoable => c.undo() },
    { (c1: Undoable, c2: Undoable) => c1 mergeWith c2 }
  ))

  /**
   * Observable stream of whether the current position within the undo manager's
   * history is the same as the last marked position.
   */
  val atMarkedPosition = EasyBind monadic undoManager flatMap new JFunction[UndoManager, ObservableValue[lang.Boolean]] {
    override def apply(t: UndoManager): ObservableValue[lang.Boolean] = t.atMarkedPositionProperty()
  }

  /**
   * Observable stream of whether undo is available
   */
  val undoAvailable = EasyBind monadic undoManager flatMap new JFunction[UndoManager, ObservableValue[lang.Boolean]] {
    override def apply(t: UndoManager): ObservableValue[lang.Boolean] = t.undoAvailableProperty()
  }

  /**
   * Observable stream of whether redo is available
   */
  val redoAvailable = EasyBind monadic undoManager flatMap new JFunction[UndoManager, ObservableValue[lang.Boolean]] {
    override def apply(t: UndoManager): ObservableValue[lang.Boolean] = t.redoAvailableProperty()
  }

  /**
   * Undo a change
   */
  def undo(): Unit = undoManager().undo()

  /**
   * Redo a change
   */
  def redo(): Unit = undoManager().redo()

  /**
   * Clears the undo history
   */
  def clearHistory(): Unit = undoManager() = UndoManagerFactory.unlimitedHistoryUndoManager(
    UndoableStream.changes,
    { c: Undoable => c.redo() },
    { c: Undoable => c.undo() },
    { (c1: Undoable, c2: Undoable) => c1 mergeWith c2 }
  )

  /**
   * Marks the current position in the undo queue
   */
  def markCurrentPosition(): Unit = undoManager().mark()
}
