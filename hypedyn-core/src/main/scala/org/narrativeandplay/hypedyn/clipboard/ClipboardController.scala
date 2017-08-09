package org.narrativeandplay.hypedyn.clipboard

import org.narrativeandplay.hypedyn.api.clipboard.Copyable

/**
 * Controller for clipboard actions
 *
 * Handles clipboard actions on a global level
 */
object ClipboardController {
  /**
   * Cuts an object to the clipboard
   *
   * @param obj The object to cut
   * @param copier The typeclass instance implementing the cut/copy/paste action for the object type
   * @tparam T The type of the object to cut
   */
  def cut[T](obj: T)(implicit copier: Copyable[T]) = copier.cut(obj)

  /**
   * Copies an object to the clipboard
   *
   * @param obj The object to copy
   * @param copier The typeclass instance implementing the cut/copy/paste actions for the object type
   * @tparam T The type of the object to copy
   */
  def copy[T](obj: T)(implicit copier: Copyable[T]) = copier.copy(obj)

  /**
   * Pastes an object from the clipboard
   *
   * @param copier The typeclass instance implementing the cut/copy/paste actions for the object type
   * @tparam T The type of the object to paste
   */
  def paste[T]()(implicit copier: Copyable[T]) = copier.paste()
}
