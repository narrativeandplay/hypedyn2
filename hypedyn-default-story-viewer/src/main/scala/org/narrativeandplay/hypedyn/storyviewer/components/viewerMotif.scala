package org.narrativeandplay.hypedyn.storyviewer.components

import javafx.beans.binding.BooleanExpression
import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Skin, Control => JfxControl}
import javafx.scene.{input => jfxsi}

import scalafx.Includes.{jfxBounds2sfx, jfxMouseEvent2sfx, jfxReadOnlyDoubleProperty2sfx}
import scalafx.beans.property.{BooleanProperty, ObjectProperty, ReadOnlyDoubleProperty}
import scalafx.event.Event
import scalafx.geometry.Bounds
import scalafx.scene.input.MouseEvent
import com.github.benedictleejh.scala.math.vector.Vector2
import org.fxmisc.easybind.EasyBind
import org.narrativeandplay.hypedyn.story.themes.MotifLike
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils._

import scalafx.scene.paint.Color

/**
  * Visual representation of a node. More accurately, the model (MVC model) for the visual representation of a node
  *
  * @param motiflike The underlying data for the node
  * @param pluginEventDispatcher The event dispatcher that is allowed to send events
  */
class ViewerMotif(motiflike: MotifLike, private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerMotif.DefaultLocation

  private val storyViewer = pluginEventDispatcher
  private val _motif = ObjectProperty(motiflike)

  /**
    * The ID of the motif
    */
  val id = motiflike.id

  /**
    * A binding for the name of the motif
    */
  val motifName = EasyBind map (_motif, (_: MotifLike).name)

  /**
    * A property determining if this motif is selected
    */
  val selected = BooleanProperty(false)

  /**
    * A property determining if this motif's content should be shown
    */
  val showContent = storyViewer.zoomLevel >= storyViewer.showContentLimit

  /**
    * A property determining if this motif's name should be shown
    */
  val showName = storyViewer.zoomLevel >= storyViewer.showLabelsLimit


  width = pluginEventDispatcher.zoomLevel() * ViewerMotif.Width
  height = pluginEventDispatcher.zoomLevel() * ViewerMotif.Height

  storyViewer.zoomLevel onChange { (_, z1, z2) =>
    val oldZoom = z1.doubleValue()
    val newZoom = z2.doubleValue()

    width = newZoom * ViewerMotif.Width
    height = newZoom * ViewerMotif.Height

    if (newZoom - oldZoom !~= 1.0) {
      val scaledFactor = newZoom / oldZoom

      relocate(scaledFactor * topLeft.x, scaledFactor * topLeft.y)
    }

    storyViewer.sizeToChildren()
  }

  skin = new ViewerMotifSkin(this)

  relocate(storyViewer.zoomLevel() * topLeft.x, storyViewer.zoomLevel() * topLeft.y)

  onMouseClicked = { me =>
    toFront()
    me.clickCount match {
      case 2 =>
        select()
        pluginEventDispatcher.requestMotifEdit(id)
      case _ =>
    }
  }

  onMousePressed = { me =>
    anchor = (me.sceneX, me.sceneY)
    topLeft = (layoutX, layoutY)

    if (selected()) deselect() else select()
    requestLayout()
  }
  onMouseDragged = { me =>
    val mouseLocationInStoryViewer = storyViewer.sceneToLocal(me.sceneX, me.sceneY)

    if (mouseLocationInStoryViewer.getX >= 0 && mouseLocationInStoryViewer.getY >= 0) {
      val translation = Vector2(me.sceneX, me.sceneY) - anchor
      val finalPos = topLeft + translation

      pluginEventDispatcher.notifyMotifMove(id, topLeft, finalPos)

      relocate(finalPos.x, finalPos.y)
      anchor = (me.sceneX, me.sceneY)
      topLeft = (layoutX, layoutY)

      storyViewer.sizeToChildren()
    }
  }

  /**
    * Returns the property containing the underlying motif data
    */
  def motif = _motif

  /**
    * Set the motif data
    *
    * @param motiflike The data to set this motif's data to
    */
  def motif_=(motiflike: MotifLike) = _motif() = motiflike

  /**
    * Returns the center point of the visual representation
    */
  def centre = topLeft + Vector2(width / 2, height / 2)

  /**
    * Move this motif to the specified point. Coordinates given refer to the upper-left corner of the motif
    *
    * @param x The x-coordinate to motif this node to
    * @param y The y-coordinate to motif this node to
    */
  override def relocate(x: Double, y: Double): Unit = {
    val clampedX = if (x <~ 0) 0 else x
    val clampedY = if (y <~ 0) 0 else y
    super.relocate(clampedX, clampedY)
    topLeft = (clampedX, clampedY)
  }

  /**
    * Select this motif
    */
  def select(): Unit = {
    selected() = true

    storyViewer.viewer.thememotiflinkGroups filter (_.themotif == this) foreach { grp =>
      grp.links foreach { l => l.select(Color.Red) }
    }

    pluginEventDispatcher.notifyMotifSelection(id)
  }

  /**
    * Unselect this motif
    */
  def deselect(): Unit = {
    selected() = false
    pluginEventDispatcher.notifyMotifDeselection(id)
  }

  /**
    * Returns the midpoints of the edges of the motif
    */
  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(0d, height / 2)

    import ViewerMotif.Edge._
    Map(Left -> (centre - widthVector),
      Right -> (centre + widthVector),
      Top -> (centre - heightVector),
      Bottom -> (centre + heightVector))
  }

  override def toString: String = s"ViewerMotif id: $id, name: ${motifName.getValue}"

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

object ViewerMotif {
  private val Width = 190d
  private val Height = 40d
  private val DefaultLocation = Vector2(100d, 100d)

  sealed trait Edge
  object Edge {
    case object Left extends Edge
    case object Right extends Edge
    case object Top extends Edge
    case object Bottom extends Edge
  }
}
