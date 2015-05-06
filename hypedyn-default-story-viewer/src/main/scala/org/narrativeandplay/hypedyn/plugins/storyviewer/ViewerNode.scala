package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.{event => jfxe}
import javafx.scene.{input => jfxsi}

import scalafx.event.Event
import scalafx.scene.input.MouseEvent

import org.narrativeandplay.hypedyn.serialisation.{SaveFloat, SaveInt, SaveHash}

import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.events.{EditNodeRequest, NodeDeselected, NodeSelected, EventBus}
import org.narrativeandplay.hypedyn.story.NodeLike
import org.narrativeandplay.hypedyn.undo.{NodeMovedChange, UndoController}

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}

//import utils.SAMConversions._
import utils.VectorImplicitConversions._

class ViewerNode(initName: String, initContent: String, val id: Long) extends Control with NodeLike {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerNode.defaultLocation

  val _name = new StringProperty(initName)
  val _content = new StringProperty(initContent)
  val _selected = BooleanProperty(false)

  width = ViewerNode.width
  height = ViewerNode.height

  relocate(topLeft.x, topLeft.y)

  setSkin(new ViewerNodeSkin(this))

  onMouseClicked = { me =>
    me.clickCount match {
      case 1 =>
        if (selected) deselect() else select()
        requestLayout()
        me.consume() // Prevent mouse event from propagating to parent pane
      case 2 => EventBus send EditNodeRequest(id)
      case _ =>
    }
  }

  onMousePressed = { me =>
    anchor = (me.sceneX, me.sceneY)
    topLeft = (getLayoutX, getLayoutY)
  }
  onMouseDragged = { me =>
    val translation = Vector2(me.getSceneX, me.getSceneY) - anchor
    val finalPos = topLeft + translation

    UndoController send new NodeMovedChange(this, topLeft, finalPos)

    relocate(finalPos.x, finalPos.y)
    anchor = (me.sceneX, me.sceneY)
    topLeft = (layoutX, layoutY)
  }

  override def relocate(x: Double, y: Double): Unit = {
    super.relocate(x, y)
    topLeft = (x, y)
  }

  override def name: String = _name()

  def name_=(newName: String): Unit = _name() = newName

  override def content: String = _content()

  def content_=(newContent: String): Unit = _content() = newContent

  def selected = _selected()

  def centre = topLeft + (ViewerNode.width / 2, ViewerNode.height / 2)

  def select(): Unit = {
    EventBus.send(NodeSelected(id))
    _selected() = true
  }

  def deselect(): Unit = {
    EventBus.send(NodeDeselected(id))
    _selected() = false
  }

  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(height / 2, 0d)

    Map("left" -> (centre - widthVector), "right" -> (centre + widthVector), "top" -> (centre - heightVector), "bottom" -> (centre + heightVector))
  }

  override def toString = s" Node(id = $id)"

  // <editor-fold desc="Utility Methods for a ScalaFX-like access pattern">

  def width = getWidth

  def width_=(value: Double) = setWidth(value)

  def height = getHeight

  def height_=(value: Double) = setHeight(value)

  def layoutX = getLayoutX
  def layoutY = getLayoutY

  def onMouseClicked = getOnMouseClicked

  //def onMouseClicked_=(value: EventHandler[_ >: jfxsi.MouseEvent]) = setOnMouseClicked(value)
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: () => Unit) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda()
    })
  }

  def onMousePressed = getOnMousePressed

  //def onMousePressed_=(value: EventHandler[_ >: jfxsi.MouseEvent]) = setOnMousePressed(value)
  def onMousePressed_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMousePressed(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def onMouseDragged = getOnMouseDragged

  //def onMouseDragged_=(value: EventHandler[_ >: jfxsi.MouseEvent]) = setOnMouseDragged(value)
  def onMouseDragged_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseDragged(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  // </editor-fold>
}

object ViewerNode {
  private val width: Double = 190
  private val height: Double = 170
  private val defaultLocation = Vector2(100d, 100d)
}
