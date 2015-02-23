package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.event.EventHandler
import javafx.scene.control.{Skin, Control}
import javafx.scene.input.MouseEvent

import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.story.Node

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, StringProperty}

import utils.SAMConversions._
import utils.VectorImplicitConversions._

class ViewerNode(initName: String, initContent: String, val id: Long) extends Control with Node {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerNode.defaultLocation

  val _name = new StringProperty(initName)
  val _content = new StringProperty(initContent)
  val _selected = BooleanProperty(false)

  width = ViewerNode.width
  height = ViewerNode.height

  relocate(topLeft.x, topLeft.y)

  setSkin(new ViewerNodeSkin(this))

  onMouseClicked = { (me: MouseEvent) =>
    if (selected) deselect() else select()
    requestLayout()
    me.consume() // Prevent mouse event from propagating to parent pane
  }

  onMousePressed = { (me: MouseEvent) =>
    anchor = (me.getSceneX, me.getSceneY)
    topLeft = (getLayoutX, getLayoutY)
  }
  onMouseDragged = { (me: MouseEvent) =>
    val translation = Vector2(me.getSceneX, me.getSceneY) - anchor
    val finalPos = topLeft + translation
    relocate(finalPos.x, finalPos.y)
    anchor = (me.getSceneX, me.getSceneY)
    topLeft = (getLayoutX, getLayoutY)
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

  def centre = topLeft + (ViewerNode.width/2, ViewerNode.height/2)

  def select(): Unit = _selected() = true
  def deselect(): Unit = _selected() = false


  // <editor-fold desc="Utility Methods for a ScalaFX-like access pattern">

  def width = getWidth
  def width_=(value: Double) = setWidth(value)
  
  def height = getHeight
  def height_=(value: Double) = setHeight(value)

  def onMouseClicked = getOnMouseClicked
  def onMouseClicked_=(value: EventHandler[_ >: MouseEvent]) = setOnMouseClicked(value)

  def onMousePressed = getOnMousePressed
  def onMousePressed_=(value: EventHandler[_ >: MouseEvent]) = setOnMousePressed(value)

  def onMouseDragged = getOnMouseDragged
  def onMouseDragged_=(value: EventHandler[_ >: MouseEvent]) = setOnMouseDragged(value)

  // </editor-fold>
}

object ViewerNode {
  private val width: Double = 190
  private val height: Double = 170
  private val defaultLocation = Vector2(100d, 100d)
}
