package org.narrativeandplay.hypedyn.clipboard

/**
 * Typeclass defining that a type can be copied to the clipboard
 * @tparam T The type that is to be copyable
 */
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
