package org.narrativeandplay.hypedyn.plugins.storyviewer

import com.github.benedictleejh.scala.math.vector.Vector2

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}

import utils.BezierCurve
import utils.VectorImplicitConversions._
import utils.DoubleUtils._

import scalafx.geometry.{Point2D, Pos}
import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.shape._

class Link(val from: ViewerNode, val to: ViewerNode, initName: String, private val parentLinkGroup: LinkGroup) {
  private var closestBezierParam = -1d

  var clickedPoint = Vector2(-1d, -1d)
  var linkPath = BezierCurve((-1d, -1d), (-1d, -1d), (-1d, -1d), (-1d, -1d))

  val _name = new StringProperty(initName)
  val _selected = BooleanProperty(false)

  def name: String = _name()
  def name_=(newName: String) = _name() = newName

  def selected = _selected()

  private val linkLabel = new Label {
    minWidth = Link.labelWidth
    minHeight = Link.labelHeight
    maxWidth = Link.labelWidth
    maxHeight = Link.labelHeight
    alignment = Pos.Center
    wrapText = true

    text <== _name
  }

  private val labelBackground = new Rectangle {
    width = Link.labelWidth
    height = Link.labelHeight
    fill = Color.LightGrey
  }

  private def computeEndPoints = {
    var minDistPoints = (Vector2(0d, 0d), Vector2(0d, 0d), Double.MaxValue)
    val fromPoints = from.edgePoints
    val toPoints = to.edgePoints

    val endPointPairs = fromPoints.flatMap { case (fromPosition, fromCoords) =>
      toPoints.map { case (toPosition, toCoords) =>
        (fromCoords, toCoords) -> (toCoords - fromCoords).length
      }
    }

    endPointPairs.foreach { case ((fromPt, toPt), dist) =>
      minDistPoints match {
        case (startPt, endPt, minDist) =>
          if (dist <~ minDist) {
            minDistPoints = (fromPt, toPt, dist)
          }
      }
    }

    minDistPoints
  }

  def select(x: Double, y: Double): Unit = {
    _selected() = true
    clickedPoint = (x, y)
    closestBezierParam = linkPath closestPointParameterValue clickedPoint
  }

  def deselect(): Unit = {
    _selected() = false
    clickedPoint = (-1d, -1d)
    closestBezierParam = -1d
  }

  def contains(x: Double, y: Double): Boolean = path contains new Point2D(x, y)

  def computePath(): Unit = {
    val endPoints = computeEndPoints

    val edgeGroupIndex = parentLinkGroup.indexOf(this)

    val startPoint = endPoints._1
    val endPoint = endPoints._2

    val x = parentLinkGroup.size
    val y = (endPoint - startPoint).length / 100
    val gap = 0.2717 * math.exp(-0.1788 * x) + 1.138 * math.exp(-1.832 * y) + 0.006112

    val f = ((1 + edgeGroupIndex) / 2) * gap
    val mainEdge = parentLinkGroup.get(0)
    val m = edgeGroupIndex + (if (from eq mainEdge.from) 0 else 1)

    var v = endPoint - startPoint
    var v2 = 0.6 *: Vector2(v.y, -v.x) //rotate v 90 deg anti-clockwise
    var o = Vector2(0d, 0d)

    v *= 0.2

    if (parentLinkGroup.size % 2 == 0) {
      o = v2 * (gap / 2)

      if(from ne mainEdge.from)
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

    linkPath = BezierCurve(startPoint, ctrlPt1, ctrlPt2, endPoint)
  }

  def path = {
    val moveTo = new MoveTo {
      x = linkPath.startPoint.x
      y = linkPath.startPoint.y
    }
    val curveTo = new CubicCurveTo {
      controlX1 = linkPath.controlPoint1.x
      controlY1 = linkPath.controlPoint1.y
      controlX2 = linkPath.controlPoint2.x
      controlY2 = linkPath.controlPoint2.y
      x = linkPath.endPoint.x
      y = linkPath.endPoint.y
    }
    val line = new Path
    line.elements.addAll(moveTo, curveTo)

    line
  }

  def graphicElements = {
    computePath()

    val line = path

    val highlight = if (selected) {
      val h = path
      h.stroke = Color.Red
      h.strokeWidth = 5
      Some(h)
    }
    else None

    val label: Option[Label] = Link.LinkNameDisplayType match {
      case OnLinkAlways =>
        val labelMidpoint = linkPath.pointAt(0.5)
        linkLabel.relocate(labelMidpoint.x - Link.labelWidth/2, labelMidpoint.y - Link.labelHeight/2)
        Some(linkLabel)
      case OnLinkOnClick =>
        if (selected) {
          val labelMidpoint = linkPath.pointAt(0.5)
          linkLabel.relocate(labelMidpoint.x - Link.labelWidth/2, labelMidpoint.y - Link.labelHeight/2)
          Some(linkLabel)
        }
        else None
      case AtMouseOnClick =>
        if (selected) {
          val labelMidpoint = linkPath.pointAt(closestBezierParam)
          linkLabel.relocate(labelMidpoint.x - Link.labelWidth/2, labelMidpoint.y - Link.labelHeight/2)
          Some(linkLabel)
        }
        else None
    }

    val labelBg: Option[Rectangle] = label.map { lbl =>
      labelBackground.relocate(lbl.layoutX(), lbl.layoutY())
      labelBackground
    }

    val tangentVector = -linkPath.gradientAt(0.85).normalise * 10
    val headToTail1 = tangentVector.rotate(30)
    val headToTail2 = tangentVector.rotate(-30)
    val triangleHead = linkPath.pointAt(0.85)
    val tail1 = triangleHead + headToTail1
    val tail2 = triangleHead + headToTail2

    val arrowhead = Polygon(
      triangleHead.x, triangleHead.y,
      tail1.x, tail1.y,
      tail2.x, tail2.y
    )
    arrowhead.fill = Color.Black


    (line, highlight, label, labelBg, arrowhead)
  }
}

object Link {
  private val labelHeight = 20
  private val labelWidth = 40

  val LinkNameDisplayType: LinkNameDisplayType = OnLinkAlways
}
