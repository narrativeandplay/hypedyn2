package org.narrativeandplay.hypedyn.storyviewer.utils

import com.github.benedictleejh.scala.math.vector.Vector2

/**
 * Class representing a generic line of the form Ax + By + C = 0 in 2 dimensions.
 *
 * @param xCoeff The coefficient of x
 * @param yCoeff The coefficient of y
 * @param const The constant
 */
case class Line(xCoeff: Double, yCoeff: Double, const: Double) {
  /**
   * Create a line given 2 points on the line
   *
   * @param pt1 The first point on the line
   * @param pt2 The second point on the line
   * @return The line passing through the 2 given points
   */
  def this(pt1: Vector2[Double], pt2: Vector2[Double]) = this(pt2.y - pt1.y,
                                                              pt1.x - pt2.x,
                                                              pt2.x * pt1.y - pt1.x * pt2.y)

  def a = xCoeff
  def b = yCoeff
  def c = const

  override def toString: String = s"${a}x + ${b}y + $c = 0"
}

object Line {
  def apply(pt1: Vector2[Double], pt2: Vector2[Double]) = new Line(pt1, pt2)
}
