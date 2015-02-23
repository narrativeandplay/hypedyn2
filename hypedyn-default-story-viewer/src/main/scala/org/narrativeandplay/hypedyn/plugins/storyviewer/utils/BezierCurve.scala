package org.narrativeandplay.hypedyn.plugins.storyviewer.utils

import com.github.benedictleejh.scala.math.vector.Vector2
import DoubleUtils._

case class BezierCurve(startPoint: Vector2[Double],
                       controlPoint1: Vector2[Double],
                       controlPoint2: Vector2[Double],
                       endPoint: Vector2[Double]) {

  def pointAt(t: Double) = {
    val c0 = math.pow(1 - t, 3)
    val c1 = 3 * math.pow(1 - t, 2) * t
    val c2 = 3 * (1 - t) * math.pow(t, 2)
    val c3 = math.pow(t, 3)

    c0 *: startPoint + c1 *: controlPoint1 + c2 *: controlPoint2 + c3 *: endPoint
  }

  def gradientAt(t: Double) = {
    val c0 = 3 * math.pow(1 - t, 2)
    val c1 = 6 * (1 - t) * t
    val c2 = 3 * math.pow(t, 2)

    c0 *: (controlPoint1 - startPoint) + c1 *: (controlPoint2 - controlPoint1) + c2 *: (endPoint - controlPoint2)
  }

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
