package org.narrativeandplay.hypedyn.clipboard

trait Copyable[T] {
  /**
   * Defines what to do on a cut action
   */
  def cut(t: T): Unit

  /**
   * Defines what to do on a copy action
   */
  def copy(t: T): Unit

  /**
   * Defines what to do on a paste action
   */
  def paste(): Unit
}
