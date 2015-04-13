package org.narrativeandplay.hypedyn.undo

import java.util.Optional

trait Change[T] {
  def undo(): Unit
  def redo(): Unit

  def merge(other: Change[_]): Option[Change[_]] = None

  def mergeWith(other: Change[_]): Optional[Change[_]] = merge(other) match {
    case Some(c) => Optional.of(c)
    case None => Optional.empty()
  }
}
