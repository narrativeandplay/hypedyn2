package org.narrativeandplay.hypedyn.storyviewer.components

import javafx.beans.binding.BooleanExpression
import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Control => JfxControl, Skin}
import javafx.scene.{input => jfxsi}

import scalafx.Includes.{jfxBounds2sfx, jfxMouseEvent2sfx, jfxReadOnlyDoubleProperty2sfx}
import scalafx.beans.property.{ReadOnlyDoubleProperty, BooleanProperty, ObjectProperty}
import scalafx.event.Event
import scalafx.geometry.Bounds
import scalafx.scene.input.MouseEvent

import com.github.benedictleejh.scala.math.vector.Vector2
import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._
import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils._

/**
 * Visual representation of a node. More accurately, the model (MVC model) for the visual representation of a node
 *
 * @param nodal The underlying data for the node
 * @param pluginEventDispatcher The event dispatcher that is allowed to send events
 */
class ViewerNode(nodal: Nodal, private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerNode.DefaultLocation

  private val storyViewer = pluginEventDispatcher
  private val _node = ObjectProperty(nodal)

  /**
   * The ID of the node
   */
  val id = nodal.id

  /**
   * A binding for the name of the node
   */
  val nodeName = EasyBind map (_node, (_: Nodal).name)

  /**
   * A binding for the text content of the node
   */
  val contentText = EasyBind map (_node, (_: Nodal).content.text)

  /**
   * A property determining if this node is selected
   */
  val selected = BooleanProperty(false)

  /**
   * A boolean expression for determining if this node is an anywhere node
   */
  val isAnywhere = BooleanExpression.booleanExpression(EasyBind map (_node, { n: Nodal => Boolean box n.isAnywhere }))

  width = pluginEventDispatcher.zoomLevel() * ViewerNode.Width
  height = pluginEventDispatcher.zoomLevel() * ViewerNode.Height

  storyViewer.zoomLevel onChange { (_, z1, z2) =>
    val oldZoom = DoubleUtils clamp (storyViewer.minZoom, storyViewer.maxZoom, z1.doubleValue())
    val newZoom = DoubleUtils clamp (storyViewer.minZoom, storyViewer.maxZoom, z2.doubleValue())

    width = newZoom * ViewerNode.Width
    height = newZoom * ViewerNode.Height

    if (newZoom - oldZoom !~= 1.0) {
      val scaledFactor = newZoom / oldZoom

      relocate(scaledFactor * topLeft.x, scaledFactor * topLeft.y)
    }
  }

  skin = new ViewerNodeSkin(this)

  relocate(topLeft.x, topLeft.y)

  onMouseClicked = { me =>
    me.clickCount match {
      case 1 =>
        if (selected()) deselect() else select()
        requestLayout()
      case 2 => pluginEventDispatcher.requestNodeEdit(id)
      case _ =>
    }
  }

  onMousePressed = { me =>
    anchor = (me.sceneX, me.sceneY)
    topLeft = (layoutX, layoutY)
  }
  onMouseDragged = { me =>
    val translation = Vector2(me.sceneX, me.sceneY) - anchor
    val finalPos = topLeft + translation

    pluginEventDispatcher.notifyNodeMove(id, topLeft, finalPos)

    relocate(finalPos.x, finalPos.y)
    anchor = (me.sceneX, me.sceneY)
    topLeft = (layoutX, layoutY)

    storyViewer.sizeToChildren()
  }

  /**
   * Returns the property containing the underlying node data
   */
  def node = _node

  /**
   * Set the node data
   *
   * @param nodal The data to set this node's data to
   */
  def node_=(nodal: Nodal) = _node() = nodal

  /**
   * Returns the center point of the visual representation
   */
  def centre = topLeft + Vector2(width / 2, height / 2)

  /**
   * Move this node to the specified point. Coordinates given refer to the upper-left corner of the node
   *
   * @param x The x-coordinate to move this node to
   * @param y The y-coordinate to move this node to
   */
  override def relocate(x: Double, y: Double): Unit = {
    super.relocate(x, y)
    topLeft = (x, y)
  }

  /**
   * Select this node
   */
  def select(): Unit = {
    selected() = true
    pluginEventDispatcher.notifyNodeSelection(id)
  }

  /**
   * Unselect this node
   */
  def deselect(): Unit = {
    selected() = false
    pluginEventDispatcher.notifyNodeDeselection(id)
  }

  /**
   * Returns the midpoints of the edges of the node
   */
  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(height / 2, 0d)

    Map("left" -> (centre - widthVector),
        "right" -> (centre + widthVector),
        "top" -> (centre - heightVector),
        "bottom" -> (centre + heightVector))
  }

  override def toString: String = s"ViewerNode id: $id, name: ${nodeName.getValue}"

  // <editor-fold desc="Utility Methods for a Scala-like access pattern">

  def width = getWidth
  def width_=(value: Double) = setWidth(value)

  def height = getHeight
  def height_=(value: Double) = setHeight(value)

  def skin = getSkin
  def skin_=(skin: Skin[_]) = setSkin(skin)

  def layoutX = getLayoutX
  def layoutY = getLayoutY

  def bounds: Bounds = getBoundsInParent

  def onMouseClicked = { me: MouseEvent => getOnMouseClicked.handle(me) }
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def onMousePressed = { me: MouseEvent => getOnMousePressed.handle(me) }
  def onMousePressed_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMousePressed(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def onMouseDragged = { me: MouseEvent => getOnMouseDragged.handle(me) }
  def onMouseDragged_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseDragged(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  // </editor-fold>
}

object ViewerNode {
  private val Width = 190d
  private val Height = 170d
  private val DefaultLocation = Vector2(100d, 100d)
}
