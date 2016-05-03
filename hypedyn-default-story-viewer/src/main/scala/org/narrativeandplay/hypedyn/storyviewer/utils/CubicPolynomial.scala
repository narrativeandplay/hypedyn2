package org.narrativeandplay.hypedyn.storyviewer.utils

import DoubleUtils._

/**
 * Class representing a cubic polynomial of the form ax^3^ + bx^2^ + cx + d
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
    /**
     * Finds a root of an function using Halley's method
     *
     * @param approximateRoot The initial guess
     * @return A root of the function
     */
    def findRoot(approximateRoot: Double): Double = {
      val value = valueAt(approximateRoot)
      val deriv = derivativeAt(approximateRoot)
      val secondDeriv = secondDerivativeAt(approximateRoot)

      val nextApproxRoot = approximateRoot - (2 * value * deriv) / (2 * deriv * deriv - value * secondDeriv)

      if (nextApproxRoot ~= approximateRoot) nextApproxRoot else findRoot(nextApproxRoot)
    }

    val A_2 = b / a
    val A_1 = c / a
    val A_0 = d / a

    val x_inflec = -A_2 / 3

    val D = A_2 * A_2 - 3 * A_1

    val f_x_inflec = valueAt(x_inflec)
    val x_1 = if (f_x_inflec ~= 0) {
      x_inflec
    }
    else if (D ~= 0) {
      x_inflec - math.cbrt(f_x_inflec)
    }
    else if (D <~ 0) {
      findRoot(x_inflec)
    }
    else if (f_x_inflec >~ 0) {
      val x_low :: _ = List(x_inflec + (2d / 3d * math.sqrt(D)),
        x_inflec - (2d / 3d * math.sqrt(D))).sorted

      findRoot(x_low)
    }
    else {
      val _ :: x_high :: _ = List(x_inflec + (2d / 3d * math.sqrt(D)),
        x_inflec - (2d / 3d * math.sqrt(D))).sorted

      findRoot(x_high)
    }

    val B_1 = x_1 + A_2
    val B_0 = B_1 * x_1 + A_1

    val discriminant = B_1 * B_1 - 4 * B_0

    if (discriminant <~ 0) {
      List(x_1)
    }
    else if (discriminant ~= 0) {
      List(x_1, -B_1/2).sorted
    }
    else {
      List(x_1, (-B_1 + math.sqrt(discriminant)) / 2, (-B_1 - math.sqrt(discriminant)) / 2).sorted
    }
  }

  def apply = valueAt _
  def valueAt(x: Double) = a * pow(x, 3) + b * pow(x, 2) + c * x + d

  def derivativeAt(x: Double) = 3 * a * pow(x, 2) + 2 * b * x + c
  def secondDerivativeAt(x: Double) = 6 * a * x + 2 * b
}
