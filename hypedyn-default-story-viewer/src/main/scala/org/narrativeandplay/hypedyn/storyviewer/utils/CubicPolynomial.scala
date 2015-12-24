package org.narrativeandplay.hypedyn.storyviewer.utils

import DoubleUtils._

/**
 * Class representing a cubic polynomial of the form ax^3 + bx^2 + cx + d
 *
 * @param a The coefficient of the cube term. This cannot be 0.
 * @param b The coefficient of the square term.
 * @param c The coefficient of the linear term.
 * @param d The constant
 */
case class CubicPolynomial(a: Double, b: Double, c: Double, d: Double) {
  assert(a != 0, "a must not be 0")
  import math.pow

  def roots: List[Double] = {
    val A_2 = b / a
    val A_1 = c / a
    val A_0 = d / a

    val x_inflec = -A_2 / 3

    val D = A_2 * A_2 - 3 * A_1


    ???
  }

  def apply = valueAt _
  def valueAt(x: Double) = a * pow(x, 3) + b * pow(x, 2) + c * x + d

}
