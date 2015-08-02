package org.narrativeandplay.hypedyn.storyviewer.components

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Control => JfxControl, Skin}
import javafx.scene.{input => jfxsi}

import scalafx.Includes.{jfxBounds2sfx, jfxMouseEvent2sfx}
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.event.Event
import scalafx.geometry.Bounds
import scalafx.scene.input.MouseEvent

import com.github.benedictleejh.scala.math.vector.Vector2
import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer
import org.narrativeandplay.hypedyn.storyviewer.utils.FunctionImplicits._
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._

class ViewerNode(nodal: Nodal, private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerNode.DefaultLocation

  private val storyViewer = pluginEventDispatcher
  private val _node = ObjectProperty(nodal)

  val id = nodal.id

  val nodeName = EasyBind map (_node, (_: Nodal).name)
  val contentText = EasyBind map (_node, (_: Nodal).content.text)
  val selected = BooleanProperty(false)
  val isAnywhere = EasyBind map (_node, { n: Nodal => Boolean box n.isAnywhere })

  width = ViewerNode.Width
  height = ViewerNode.Height

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

  def node = _node
  def node_=(nodal: Nodal) = _node() = nodal

  def centre = topLeft + Vector2(ViewerNode.Width / 2, ViewerNode.Height / 2)

  override def relocate(x: Double, y: Double): Unit = {
    super.relocate(x, y)
    topLeft = (x, y)
  }

  def select(): Unit = {
    selected() = true
    pluginEventDispatcher.notifyNodeSelection(id)
  }

  def deselect(): Unit = {
    selected() = false
    pluginEventDispatcher.notifyNodeDeselection(id)
  }

  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(height / 2, 0d)

    Map("left" -> (centre - widthVector),
        "right" -> (centre + widthVector),
        "top" -> (centre - heightVector),
        "bottom" -> (centre + heightVector))
  }

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
