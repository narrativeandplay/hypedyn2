package org.narrativeandplay.hypedyn.storyviewer.components

import scalafx.Includes.jfxObservableValue2sfx
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.geometry.{Point2D, Pos}
import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Polygon, Rectangle}
import scalafx.scene.text.Text

import com.github.benedictleejh.scala.math.vector.Vector2
import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.BezierCurve
import org.narrativeandplay.hypedyn.story.rules.RuleLike
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils._
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._

/**
 * Class representing a link
 *
 * @param from The node the link starts from
 * @param to The node the link ends at
 * @param initRule The rule representing the link
 * @param parentLinkGroup The group of links the link belongs to
 */
class Link(val from: ViewerNode,
           val to: ViewerNode,
           initRule: RuleLike,
           private val parentLinkGroup: LinkGroup) {
  private var closestBezierParam = -1d

  /**
   * The rule representing the link, wrapped in a property to allow easy updating
   */
  val rule = ObjectProperty(initRule)

  /**
   * A binding to the name of the rule
   */
  val name = EasyBind map (rule, (_: RuleLike).name)

  /**
   * A property determining if the link is currently selected
   */
  val selected = BooleanProperty(false)

  private val linkLabel = new Label {
    prefWidth <== EasyBind map (name, { s: String => Double box new Text(s).layoutBounds().getWidth })
    maxWidth = Link.LabelWidth
    maxHeight = Link.LabelHeight
    alignment = Pos.Center
    wrapText = true

    text <== name
  }

  private val labelBackground = new Rectangle {
    width <== linkLabel.width
    height = Link.LabelHeight
    fill = Link.DefaultBackgroundColour
  }

  private def endPoints = {
    val fromPoints = from.edgePoints
    val toPoints = to.edgePoints

    val endPointPairs = fromPoints flatMap { case (fromPosition, fromCoords) =>
      toPoints map { case (toPosition, toCoords) =>
        (fromPosition, fromCoords, toPosition, toCoords, (toCoords - fromCoords).length)
      }
    }

    endPointPairs minBy (_._5) match { case minPtPair @ (_, fromCoords, _, toCoords, length) =>
      (fromCoords, toCoords, length)
    }
  }

  /**
   * Setter for updating the link's rule
   *
   * @param newRule The new data for the rule
   */
  def rule_=(newRule: RuleLike) = rule() = newRule

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

    val (startPoint, endPoint, _) = endPoints

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
    val line = path.toFxPath

    val highlight = if (selected()) {
      val h = path.toFxPath
      h.stroke = Color.Red
      h.strokeWidth = 5
      Some(h)
    }
    else None

    import Link.NameDisplayType._
    val label: Option[Label] = Link.NameDisplay match {
      case OnLinkAlways =>
        val labelMidpoint = path pointAt 0.5
        linkLabel.relocate(labelMidpoint.x - Link.LabelWidth / 2, labelMidpoint.y - Link.LabelHeight / 2)
        Some(linkLabel)
      case OnLinkOnClick =>
        if (selected()) {
          val labelMidpoint = path pointAt 0.5
          linkLabel.relocate(labelMidpoint.x - Link.LabelWidth / 2, labelMidpoint.y - Link.LabelHeight / 2)
          Some(linkLabel)
        }
        else None
      case AtMouseOnClick =>
        if (selected()) {
          val labelMidpoint = path pointAt closestBezierParam
          linkLabel.relocate(labelMidpoint.x - Link.LabelWidth / 2, labelMidpoint.y - Link.LabelHeight / 2)
          Some(linkLabel)
        }
        else None
    }

    val labelBg: Option[Rectangle] = label.map { lbl =>
      labelBackground.relocate(lbl.layoutX(), lbl.layoutY())
      labelBackground
    }

    val tangentVector = -(path gradientAt 0.85).normalise * 10
    val headToTail1 = tangentVector rotate 30
    val headToTail2 = tangentVector rotate -30
    val triangleHead = path pointAt 0.85
    val tail1 = triangleHead + headToTail1
    val tail2 = triangleHead + headToTail2

    val arrowhead = Polygon(triangleHead.x, triangleHead.y,
                            tail1.x, tail1.y,
                            tail2.x, tail2.y)
    arrowhead.fill = Color.Black


    (line, highlight, label, labelBg, arrowhead)
  }
}

object Link {
  private val LabelHeight = 20
  private val LabelWidth = 100
  private val DefaultBackgroundColour = Color.web("#f4f4f4")

  val NameDisplay: NameDisplayType = NameDisplayType.OnLinkAlways

  /**
   * Enumeration for the ways to display the link name
   */
  sealed trait NameDisplayType
  object NameDisplayType {
    case object AtMouseOnClick extends NameDisplayType
    case object OnLinkAlways extends NameDisplayType
    case object OnLinkOnClick extends NameDisplayType
  }
}
