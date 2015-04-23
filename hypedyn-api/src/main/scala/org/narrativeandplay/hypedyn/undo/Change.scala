package org.narrativeandplay.hypedyn.undo

import java.util.Optional

trait Change {
  def undo(): Unit
  def redo(): Unit

  def merge(other: Change): Option[Change] = None

  def mergeWith(other: Change): Optional[Change] = merge(other) match {
    case Some(c) => Optional.of(c)
    case None => Optional.empty()
  }
}
