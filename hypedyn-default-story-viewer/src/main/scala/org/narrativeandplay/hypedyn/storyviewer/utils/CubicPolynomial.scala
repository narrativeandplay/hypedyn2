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

/**
 * A complex number of the form a + ib
 */
sealed case class ComplexNumber(a: Double, b: Double) {
  def conjugate = ComplexNumber(a, -b)
  def unary_~ = conjugate
  def absSqr = a*a + b*b
  def abs = math.sqrt(absSqr)

  def +(z: ComplexNumber) = ComplexNumber(a + z.a, b + z.b)
  def -(z: ComplexNumber) = ComplexNumber(a - z.a, b - z.b)

  def *(z: ComplexNumber) = ComplexNumber(a * z.a - b * z.b, b * z.a + a * z.b)
  def /(z: ComplexNumber) = ComplexNumber((a * z.a + b * z.b)/z.absSqr, (b * z.a - a * z.b)/z.absSqr)

  def isReal = b ~= 0
  def isComplex = !isReal

  def toDouble = if (isReal) a else abs

  override def toString = s"$a + ${b}i"

  override def equals(obj: Any): Boolean = {
    case z: ComplexNumber => (a ~= z.a) && (b ~= z.b)
    case _ => false
  }
}

object ComplexNumber {
  def apply(d: Double) = ComplexNumber(d, 0)
}
