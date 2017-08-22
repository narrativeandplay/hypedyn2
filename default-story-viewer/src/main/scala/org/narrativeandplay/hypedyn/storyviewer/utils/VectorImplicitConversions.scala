package org.narrativeandplay.hypedyn.storyviewer.utils

import java.awt.Point

import scala.language.implicitConversions

import scalafx.geometry.Point2D

/**
 * Convenience implicit conversions to and from vectors
 */
object VectorImplicitConversions {
  implicit def Tuple2ToVector2[T](t: (T, T))(implicit n: Numeric[T]): Vector2[T] = Vector2[T](t._1, t._2)

  implicit def pointToVector2Int(p: Point): Vector2[Int] = Vector2(p.x, p.y)

  implicit def point2dToVector2Double(p: Point2D): Vector2[Double] = Vector2(p.getX, p.getY)

  implicit def vector2Double2Point2D(p: Vector2[Double]): Point2D = new Point2D(p.x, p.y)

  implicit class RotatableVector(v: Vector2[Double]) {
    private def degToRad(x: Double) = math.Pi / 180 * x

    def rotate(deg: Double) = Vector2(math.cos(degToRad(deg)) * v.x - math.sin(degToRad(deg)) * v.y,
                                      math.sin(degToRad(deg)) * v.x + math.cos(degToRad(deg)) * v.y)
  }

}
