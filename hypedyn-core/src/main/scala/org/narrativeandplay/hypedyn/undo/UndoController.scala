package org.narrativeandplay.hypedyn.undo

import java.lang
import java.util.Optional
import java.util.function.{Function => JFunction, BiFunction, Consumer}
import javafx.beans.value.ObservableValue

import scalafx.beans.property.ObjectProperty

import org.fxmisc.easybind.EasyBind
import org.fxmisc.undo.{UndoManager, UndoManagerFactory}

import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._

/**
 * Controller handling undo events
 */
object UndoController {
  private val undoManager = ObjectProperty(makeUndoManager)

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
  def clearHistory(): Unit = undoManager() = makeUndoManager

  /**
   * Marks the current position in the undo queue
   */
  def markCurrentPosition(): Unit = undoManager().mark()

  private def makeUndoManager = UndoManagerFactory.unlimitedHistoryUndoManager[Undoable](
    UndoableStream.changes,
    { u: Undoable => u.undo() },
    { undoable: Undoable => undoable.redo() },
    { (u1: Undoable, u2: Undoable) => u1 mergeWith u2}
  )
}
