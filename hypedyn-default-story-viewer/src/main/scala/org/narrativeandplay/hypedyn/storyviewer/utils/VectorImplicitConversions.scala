package org.narrativeandplay.hypedyn.storyviewer.utils

import java.awt.Point
import java.awt.geom.Point2D

import scala.language.implicitConversions

import com.github.benedictleejh.scala.math.vector.{Vector2, VectorN}

/**
 * Convenience implicit conversions to and from vectors
 */
object VectorImplicitConversions {
  implicit def Tuple2ToVector2[T](t: (T, T))(implicit n: Numeric[T]): Vector2[T] = new Vector2[T](t._1, t._2)

  implicit def pointToVector2Int(p: Point): Vector2[Int] = new Vector2(p.x, p.y)

  implicit def point2dToVector2Double(p: Point2D): Vector2[Double] = new Vector2(p.getX, p.getY)

  implicit def vectorNIntToPoint(v: VectorN[Int]): Point = {
    v.dimension match {
      case 2 =>
        val v2 = v.asInstanceOf[Vector2[Int]]
        new Point(v2.x, v2.y)
      case _ => throw new ClassCastException
    }
  }

  implicit class RotatableVector(v: Vector2[Double]) {
    private def degToRad(x: Double) = math.Pi / 180 * x

    def rotate(deg: Double) = Vector2(math.cos(degToRad(deg)) * v.x - math.sin(degToRad(deg)) * v.y,
                                      math.sin(degToRad(deg)) * v.x + math.cos(degToRad(deg)) * v.y)
  }

}
