package org.narrativeandplay.hypedyn.storyviewer.utils

/**
 * Class representing a cubic polynomial
 *
 * @param a The coefficient of the cube term. This cannot be 0.
 * @param b The coefficient of the square term.
 * @param c The coefficient of the linear term.
 * @param d The constant
 */
case class CubicPolynomial(a: Double, b: Double, c: Double, d: Double) {
  assert(a != 0, "a must not be 0")

  def roots: List[Double] = {
    import math.pow
    val discriminant = 18 * a * b * c * d -
      4 * pow(b, 3) * d +
      pow(b, 2) * pow(c, 2) -
      4 * a * pow(c, 3) -
      27 * pow(a, 2) * pow(d, 2)
    ???
  }
}

/**
 * A complex number of the form a + ib
 */
sealed case class ComplexNumber(a: Double, b: Double) {
  def conjugate = ComplexNumber(a, -b)
  def unary_~ = conjugate
  def abs = math.sqrt(a*a + b*b)

  def +(z: ComplexNumber) = ComplexNumber(a + z.a, b + z.b)
  def -(z: ComplexNumber) = ComplexNumber(a - z.a, b - z.b)

  def *(z: ComplexNumber) = ComplexNumber(a * z.a - b * z.b, b * z.a + a * z.b)
}

object ComplexNumber {
  def fromDouble(d: Double) = ComplexNumber(d, 0)
}
