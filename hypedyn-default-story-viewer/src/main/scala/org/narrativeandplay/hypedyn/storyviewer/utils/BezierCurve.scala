package org.narrativeandplay.hypedyn.storyviewer.utils

import scalafx.scene.shape.{CubicCurveTo, MoveTo, Path}

import com.github.benedictleejh.scala.math.vector.Vector2

import DoubleUtils._

/**
 * Class representing a cubic Bezier curve
 *
 * @param startPoint The starting coordinate for the curve
 * @param controlPoint1 The coordinates of first control point for the curve
 * @param controlPoint2 The coordinates of second control point for the curve
 * @param endPoint The ending coordinate for the curve
 */
case class BezierCurve(startPoint: Vector2[Double],
                       controlPoint1: Vector2[Double],
                       controlPoint2: Vector2[Double],
                       endPoint: Vector2[Double]) {

  /**
   * Get the x-y coordinate of the given parameter along the curve
   *
   * @param t The parameter of the point along the curve, from 0 to 1
   * @return The x-y coordinate of the point for the given parameter
   */
  def pointAt(t: Double) = {
    val c0 = math.pow(1 - t, 3)
    val c1 = 3 * math.pow(1 - t, 2) * t
    val c2 = 3 * (1 - t) * math.pow(t, 2)
    val c3 = math.pow(t, 3)

    c0 *: startPoint + c1 *: controlPoint1 + c2 *: controlPoint2 + c3 *: endPoint
  }

  /**
   * Gets the gradient vector of a given parameter along the curve
   *
   * @param t The parameter of the point along the curve, from 0 to 1
   * @return The gradient vector of the point along the curve for the given parameter
   */
  def gradientAt(t: Double) = {
    val c0 = 3 * math.pow(1 - t, 2)
    val c1 = 6 * (1 - t) * t
    val c2 = 3 * math.pow(t, 2)

    c0 *: (controlPoint1 - startPoint) + c1 *: (controlPoint2 - controlPoint1) + c2 *: (endPoint - controlPoint2)
  }

  /**
   * Calculates the parameter of the curve point closest to the given point
   *
   * @param point The point to compute the parameter value for
   * @return The parameter value for the point on the curve closest to the given point
   */
  def closestPointParameterValue(point: Vector2[Double]) = {
    var tMin = 0.0
    var tMax = 1.0

    while (tMin !~= tMax) {
      val minPt = pointAt(tMin) - point
      val maxPt = pointAt(tMax) - point

      val minLength = minPt.length
      val maxLength = maxPt.length

      val halfwayT = (tMin + tMax) / 2
      if (minLength <~ maxLength) tMax = halfwayT else tMin = halfwayT
    }

    tMin
  }
}

object BezierCurve {

  /**
   * Implicit class for convenience methods to convert a Bezier curve into a JavaFX path
   *
   * @param curve The curve to provide the convenience methods for
   */
  implicit class UiBezierCurve(curve: BezierCurve) {
    /**
     * Returns the JavaFX `Path` for the given Bezier curve
     */
    def toFxPath = {
      val moveTo = new MoveTo {
        x = curve.startPoint.x
        y = curve.startPoint.y
      }
      val curveTo = new CubicCurveTo {
        controlX1 = curve.controlPoint1.x
        controlY1 = curve.controlPoint1.y
        controlX2 = curve.controlPoint2.x
        controlY2 = curve.controlPoint2.y
        x = curve.endPoint.x
        y = curve.endPoint.y
      }
      val line = new Path()
      line.elements.addAll(moveTo, curveTo)

      line
    }
  }
}
