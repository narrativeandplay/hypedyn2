package org.narrativeandplay.hypedyn.undo

import java.util.function.{BiFunction, Consumer}

import scala.language.implicitConversions

import org.fxmisc.undo.UndoManagerFactory
import org.reactfx.EventSource

object UndoController {
  implicit def lambdaToConsumer[C](lambda: C => Unit): Consumer[C] = new Consumer[C] {
    override def accept(t: C): Unit = lambda(t)
  }

  implicit def lambdaToBiFunction[T, U, R](lambda: (T, U) => R): BiFunction[T, U, R] = new BiFunction[T, U, R] {
    override def apply(t: T, u: U): R = lambda(t, u)
  }

  private val changes = new EventSource[Change]()

  def send(change: Change) = changes push change

  private val undoManager = UndoManagerFactory.unlimitedHistoryUndoManager(
    changes,
    { c: Change =>
      c.redo()
    },
    { c: Change =>
      c.undo()
    },
    { (c1: Change, c2: Change) =>
      c1 mergeWith c2
    }
  )

  def undo() = undoManager.undo()

  def redo() = undoManager.redo()

  def makeUndoableAction[T, U](action: T => U) = { (param: T, undoable: Boolean) =>
    val result = action(param)


  }
}
