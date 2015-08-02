package org.narrativeandplay.hypedyn.storyviewer.utils

import java.util.function.{Function => JFunction}

import scala.language.implicitConversions

object FunctionImplicits {
  implicit def function1ToFunction[T, U](f: T => U): JFunction[T, U] = new JFunction[T, U] {
    override def apply(t: T): U = f(t)
  }
}
