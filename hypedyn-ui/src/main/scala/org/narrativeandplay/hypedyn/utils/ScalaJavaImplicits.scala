package org.narrativeandplay.hypedyn.utils

import java.util.function.{Function => JFunction, BiFunction}

import scala.language.implicitConversions

/**
 * Implicit conversions from Scala function types to Java function types
 */
object ScalaJavaImplicits {
  implicit def function1ToFunction[T, U](f: T => U): JFunction[T, U] = new JFunction[T, U] {
    override def apply(t: T): U = f(t)
  }

  implicit def function2ToBiFunction[T, U, R](f: (T, U) => R): BiFunction[T, U, R] = new BiFunction[T, U, R] {
    override def apply(t: T, u: U): R = f(t, u)
  }
}
