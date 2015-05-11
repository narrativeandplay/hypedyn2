package org.narrativeandplay.hypedyn.undo

import java.util.Optional

trait Undoable {
  /**
   * Defines what to do when an undo action happens
   */
  def undo(): Unit

  /**
   * Defines how to reverse an undo action
   */
  def redo(): Unit

  /**
   * Combines this undoable change with another. By default, changes are not combined
   *
   * @param change The change to combine with this change
   * @return An option containing the combined change, or None is the 2 changes cannot be combined
   */
  def merge(change: Undoable): Option[Undoable] = None

  /**
   * Combines this change with another. By default, changes are not combined.
   *
   * This function is meant to handle conversion between Java `Optional`s and Scala `Option`s. It is not meant to be
   * overwritten, and delegates the actual merging to the Scala version of this function
   *
   * @param change The change to combine with this change
   * @return An `Optional` containing the combined change, or `Optional.empty()` is the 2 changes cannot be combined
   */
  def mergeWith(change: Undoable): Optional[Undoable] = merge(change) match {
    case Some(c) => Optional.of(c)
    case None => Optional.empty()
  }
}
