package org.narrativeandplay.hypedyn.storyviewer.utils

import scala.math.sqrt

case class Vector2[T](x: T, y: T)(implicit numeric: Numeric[T]) {
  import numeric.mkNumericOps

  def length: Double = sqrt((x * x + y * y).toDouble())

  def normalise[U](implicit numberLike: NumberLike[T, Double, U], numeric: Numeric[U]): Vector2[U] = this / length

  def dot[U, V](that: Vector2[U])(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): V =
    numeric.plus(
      numberLike.times(x, that.x),
      numberLike.times(y, that.y)
    )

  def unary_- = Vector2(-x, -y)

  def +[U, V](that: Vector2[U])(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): Vector2[V] =
    Vector2(numberLike.plus(x, that.x), numberLike.plus(y, that.y))

  def -[U, V](that: Vector2[U])(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): Vector2[V] =
    Vector2(numberLike.minus(x, that.x), numberLike.minus(y, that.y))

  def *[U, V](c: U)(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): Vector2[V] =
    Vector2(numberLike.times(x, c), numberLike.times(y, c))
  def *:[U, V](c: U)(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): Vector2[V] = this * c

  def /[U, V](c: U)(implicit numberLike: NumberLike[T, U, V], numeric: Numeric[V]): Vector2[V] =
    Vector2(numberLike.divide(x, c), numberLike.divide(y, c))

  override def toString = s"<$x, $y>"
}
