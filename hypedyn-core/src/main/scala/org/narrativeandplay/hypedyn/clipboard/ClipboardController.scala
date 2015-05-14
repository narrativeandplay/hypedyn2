package org.narrativeandplay.hypedyn.clipboard

object ClipboardController {
  def cut[T](obj: T)(implicit copier: Copyable[T]) = copier.cut(obj)
  def copy[T](obj: T)(implicit copier: Copyable[T]) = copier.copy(obj)
  def paste[T]()(implicit copier: Copyable[T]) = copier.paste()
}
