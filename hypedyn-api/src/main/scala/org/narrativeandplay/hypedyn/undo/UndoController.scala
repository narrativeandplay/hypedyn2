package org.narrativeandplay.hypedyn.undo

import java.util.Optional
import java.util.function.{BiFunction, BiConsumer, Consumer}

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

  private val changes = new EventSource[Change[_]]()

  def send(change: Change[_]) = changes push change

  UndoManagerFactory.unlimitedHistoryUndoManager(
    changes,
    { c: Change[_] =>
      c.redo()
    },
    { c: Change[_] =>
      c.undo()
    },
    { (c1: Change[_], c2: Change[_]) =>
      c1 mergeWith c2
    }
  )
}
