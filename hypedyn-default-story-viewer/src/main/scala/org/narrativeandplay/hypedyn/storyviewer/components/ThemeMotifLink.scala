package org.narrativeandplay.hypedyn.storyviewer.components

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.geometry.{Point2D, Pos}
import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Polygon, Rectangle}
import scalafx.scene.text.Text

import com.github.benedictleejh.scala.math.vector.Vector2
import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.storyviewer.utils.{BezierCurve, CubicPolynomial, Line}
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.{CubicPolynomial, Line, BezierCurve}
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions.StoryViewerRule
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils._
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._

/**
  * Class representing a link
  *
  * @param from The motif the link starts from
  * @param to The theme the link ends at
  * @param parentLinkGroup The group of links the link belongs to
  */
class ThemeMotifLink(val from: ViewerMotif,
                        val to: ViewerTheme,
                        private val parentLinkGroup: ThemeMotifLinkGroup) {
  private var closestBezierParam = -1d

  val name = "Connotes"

  /**
    * A property determining if the link is currently selected
    */
  val selected = BooleanProperty(false)

  private val linkLabel = new Label {
    prefWidth =  new Text(name).layoutBounds().width
    maxWidth = ThemeMotifLink.LabelWidth
    maxHeight = ThemeMotifLink.LabelHeight
    alignment = Pos.Center
    wrapText = true

    text = name

    visible <== from.showName
  }

  private val labelBackground = new Rectangle {
    width <== linkLabel.width
    height = ThemeMotifLink.LabelHeight
    fill = ThemeMotifLink.DefaultBackgroundColour

    visible <== from.showName
  }

  private def endPoints = {
    val fromPoints = from.edgePoints
    val toPoints = to.edgePoints

    val endPointPairs = fromPoints flatMap { case (fromPosition, fromCoords) =>
      toPoints map { case (toPosition, toCoords) =>
        (fromPosition, fromCoords, toPosition, toCoords, (toCoords - fromCoords).length)
      }
    }

    endPointPairs minBy (_._5) match { case minPtPair @ (fromPosition, fromCoords, toPosition, toCoords, _) =>
      (fromPosition, fromCoords, toPosition, toCoords)
    }
  }

  /**
    * Select this link
    *
    * @param x The x-position of the selection point
    * @param y The y-position of the selection point
    */
  def select(x: Double, y: Double): Unit = {
    selected() = true
    closestBezierParam = path closestPointParameterValue ((x, y))
  }

  /**
    * Select this link
    *
    * @param pt The selection point
    */
  def select(pt: Point2D): Unit = select(pt.x, pt.y)

  /**
    * Unselect this link
    */
  def deselect(): Unit = {
    selected() = false
    closestBezierParam = -1d
  }

  /**
    * Checks if the link contains the given point
    *
    * @param x The x-coordinate of the point
    * @param y The y-coordinate of the point
    * @return `true` if the link contains the given point, false otherwise
    */
  def contains(x: Double, y: Double): Boolean = mousingPath contains (x, y)

  /**
    * Checks if the link contains the given point
    *
    * @param pt The point to check
    * @return `true` if the link contains the given point, false otherwise
    */
  def contains(pt: Point2D): Boolean = mousingPath contains pt

  private def mousingPath = {
    val h = path.toFxPath
    h.strokeWidth = 5
    h
  }

  /**
    * Returns the Bezier curve (the start, end, and control points) of this link
    */
  def path = {
    val edgeGroupIndex = parentLinkGroup.indexOf(this)

    val (_, startPoint, _, endPoint) = endPoints

    val x = parentLinkGroup.size
    val y = (endPoint - startPoint).length / 100
    val gap = 0.2717 * math.exp(-0.1788 * x) + 1.138 * math.exp(-1.832 * y) + 0.006112

    val f = ((1 + edgeGroupIndex) / 2) * gap
    val mainEdge = parentLinkGroup.get(0)
    val m = edgeGroupIndex + (if (from eq mainEdge.from) 0 else 1)

    var v = endPoint - startPoint
    var v2 = 0.6 *: (v rotate 90)
    var o = Vector2(0d, 0d)

    v *= 0.2

    if (parentLinkGroup.size % 2 == 0) {
      o = v2 * (gap / 2)

      if (from ne mainEdge.from)
        o = -o
    }

    v2 *= f

    var ctrlPt1 = from.centre + v
    var ctrlPt2 = to.centre - v

    if (m % 2 == 0) {
      ctrlPt1 += (v2 + o)
      ctrlPt2 += (v2 + o)
    }
    else {
      ctrlPt1 -= (v2 - o)
      ctrlPt2 -= (v2 - o)
    }

    BezierCurve(startPoint, ctrlPt1, ctrlPt2, endPoint)
  }

  /**
    * Returns the full graphical representation of the link
    */
  def draw = {
    val pathCurve = path
    val line = pathCurve.toFxPath

    val highlight = if (selected()) {
      val h = pathCurve.toFxPath
      h.stroke = Color.Red
      h.strokeWidth = 5

      Some(h)
    }
    else None

    val labelMidpoint = pathCurve pointAt 0.5
    val label = Some(linkLabel)
    linkLabel.relocate(labelMidpoint.x - ThemeMotifLink.LabelWidth / 2, labelMidpoint.y - ThemeMotifLink.LabelHeight / 2)

    val labelBg: Option[Rectangle] = label.map { lbl =>
      labelBackground.relocate(lbl.layoutX(), lbl.layoutY())
      labelBackground
    }

    val h = Vector2(0d, to.height)
    val v = Vector2(to.width, 0d)
    val x_3 = pathCurve.endPoint.x
    val x_2 = pathCurve.controlPoint2.x
    val x_1 = pathCurve.controlPoint1.x
    val x_0 = pathCurve.startPoint.x
    val y_3 = pathCurve.endPoint.y
    val y_2 = pathCurve.controlPoint2.y
    val y_1 = pathCurve.controlPoint1.y
    val y_0 = pathCurve.startPoint.y
    import ViewerTheme.Edge._
    val potentialArrowheadLocations = to.edgePoints flatMap { case (edge, midpoint) =>
      val edgeLine = edge match {
        case Left | Right => Line(midpoint, midpoint + h)
        case Top | Bottom => Line(midpoint, midpoint + v)
      }

      val A = edgeLine.a
      val B = edgeLine.b

      val a = (A * (-x_0 + 3 * x_1 - 3 * x_2 + x_3)) + (B * (-y_0 + 3 * y_1 - 3 * y_2 + y_3))
      val b = (A * (3 * x_0 - 6 * x_1 + 3 * x_2)) + B * (3 * y_0 - 6 * y_1 + 3 * y_2)
      val c = (A * (-3 * x_0 + 3 * x_1)) + B * (-3 * y_0 + 3 * y_1)
      val d = A * x_0 + B * y_0 + edgeLine.c

      if (a ~= 0) Nil else CubicPolynomial(a, b, c, d).roots
    }

    val validArrowheadLocations = potentialArrowheadLocations filter { t =>
      (t >~= 0) && (t <~= 1.0001) && // remove out of bound values for a Bezier curve
        (to.bounds contains (pathCurve pointAt t)) // because lines extend to infinity,
      // ensure that the point lies on the end point node
    }

    // Put the arrowhead slightly before the intersection to increase its visibility
    val t = (validArrowheadLocations.toList.sorted.headOption getOrElse 1d) - 0.008
    val tangentVector = -(pathCurve gradientAt t).normalise * 10
    val headToTail1 = tangentVector rotate 30
    val headToTail2 = tangentVector rotate -30
    val triangleHead = pathCurve pointAt t
    val tail1 = triangleHead + headToTail1
    val tail2 = triangleHead + headToTail2

    val arrowhead =
      Polygon(triangleHead.x, triangleHead.y,
        tail1.x, tail1.y,
        tail2.x, tail2.y)

    (line, highlight, label, labelBg, arrowhead)
  }
}

object ThemeMotifLink {
  private val LabelHeight = 20
  private val LabelWidth = 100
  private val DefaultBackgroundColour = Color.web("#f4f4f4")
}
