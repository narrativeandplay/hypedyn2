package org.narrativeandplay.hypedyn.plugins.storyviewer.components

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.{input => jfxsi}

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.event.Event
import scalafx.scene.input.MouseEvent

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.plugins.storyviewer.StoryViewer
import org.narrativeandplay.hypedyn.plugins.storyviewer.utils.VectorImplicitConversions._
import org.narrativeandplay.hypedyn.story.NodeId

class ViewerNode(initName: String,
                 initContent: String,
                 val id: NodeId,
                 private val eventDispatcher: StoryViewer) extends Control {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerNode.defaultLocation

  val nameProperty = StringProperty(initName)
  val contentProperty = StringProperty(initContent)
  val selectedProperty = BooleanProperty(false)

  width = ViewerNode.width
  height = ViewerNode.height

  relocate(topLeft.x, topLeft.y)

  onMouseClicked = { me =>
    me.clickCount match {
      case 1 =>
        if (selected) deselect() else select()
        requestLayout()
      case 2 => eventDispatcher.requestNodeEdit(id)
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

    eventDispatcher.notifyNodeMove(id, topLeft, finalPos)

    relocate(finalPos.x, finalPos.y)
    anchor = (me.sceneX, me.sceneY)
    topLeft = (layoutX, layoutY)
  }

  def name = nameProperty()
  def name_=(newName: String) = nameProperty() = newName

  def content = contentProperty()
  def content_=(newContent: String) = contentProperty() = newContent

  def selected = selectedProperty()

  def centre = topLeft + Vector2(ViewerNode.width / 2, ViewerNode.height / 2)

  override def relocate(x: Double, y: Double): Unit = {
    super.relocate(x, y)
    topLeft = (x, y)
  }

  def select(): Unit = {
    selectedProperty() = true
    eventDispatcher.notifyNodeSelection(id)
  }

  def deselect(): Unit = {
    selectedProperty() = false
    eventDispatcher.notifyNodeDeselection(id)
  }

  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(height / 2, 0d)

    Map("left" -> (centre - widthVector), "right" -> (centre + widthVector), "top" -> (centre - heightVector), "bottom" -> (centre + heightVector))
  }

  // <editor-fold desc="Utility Methods for a Scala-like access pattern">

  def width = getWidth

  def width_=(value: Double) = setWidth(value)

  def height = getHeight

  def height_=(value: Double) = setHeight(value)

  def layoutX = getLayoutX
  def layoutY = getLayoutY

  def onMouseClicked = getOnMouseClicked
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def onMousePressed = getOnMousePressed
  def onMousePressed_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMousePressed(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def onMouseDragged = getOnMouseDragged
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
