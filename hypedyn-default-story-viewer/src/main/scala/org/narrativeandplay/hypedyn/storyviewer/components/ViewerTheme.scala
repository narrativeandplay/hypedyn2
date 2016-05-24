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
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._
import org.narrativeandplay.hypedyn.story.themes.ThemeLike
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer
import org.narrativeandplay.hypedyn.storyviewer.utils.VectorImplicitConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils._

import scalafx.scene.paint.Color

/**
  * Visual representation of a theme. More accurately, the model (MVC model) for the visual representation of a theme
  *
  * @param themelike The underlying data for the theme
  * @param pluginEventDispatcher The event dispatcher that is allowed to send events
  */
class ViewerTheme(themelike: ThemeLike, private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  private var anchor = Vector2(0.0, 0.0)
  private var topLeft = ViewerTheme.DefaultLocation

  private val storyViewer = pluginEventDispatcher
  private val _theme = ObjectProperty(themelike)

  /**
    * The ID of the theme
    */
  val id = themelike.id

  /**
    * A binding for the name of the theme
    */
  val themeName = EasyBind map (_theme, (_: ThemeLike).name)

  /**
    * A property determining if this theme is selected
    */
  val selected = BooleanProperty(false)

  /**
    * A property determining if this theme's content should be shown
    */
  val showContent = storyViewer.zoomLevel >= storyViewer.showContentLimit

  /**
    * A property determining if this theme's name should be shown
    */
  val showName = storyViewer.zoomLevel >= storyViewer.showLabelsLimit


  width = pluginEventDispatcher.zoomLevel() * ViewerTheme.Width
  height = pluginEventDispatcher.zoomLevel() * ViewerTheme.Height

  storyViewer.zoomLevel onChange { (_, z1, z2) =>
    val oldZoom = z1.doubleValue()
    val newZoom = z2.doubleValue()

    width = newZoom * ViewerTheme.Width
    height = newZoom * ViewerTheme.Height

    if (newZoom - oldZoom !~= 1.0) {
      val scaledFactor = newZoom / oldZoom

      relocate(scaledFactor * topLeft.x, scaledFactor * topLeft.y)
    }

    storyViewer.sizeToChildren()
  }

  skin = new ViewerThemeSkin(this)

  relocate(storyViewer.zoomLevel() * topLeft.x, storyViewer.zoomLevel() * topLeft.y)

  onMouseClicked = { me =>
    toFront()
    me.clickCount match {
      case 2 =>
        select()
        pluginEventDispatcher.requestThemeEdit(id)
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

      pluginEventDispatcher.notifyThemeMove(id, topLeft, finalPos)

      relocate(finalPos.x, finalPos.y)
      anchor = (me.sceneX, me.sceneY)
      topLeft = (layoutX, layoutY)

      storyViewer.sizeToChildren()
    }
  }

  /**
    * Returns the property containing the underlying theme data
    */
  def theme = _theme

  /**
    * Set the theme data
    *
    * @param themelike The data to set this theme's data to
    */
  def theme_=(themelike: ThemeLike) = _theme() = themelike

  /**
    * Returns the center point of the visual representation
    */
  def centre = topLeft + Vector2(width / 2, height / 2)

  /**
    * Move this theme to the specified point. Coordinates given refer to the upper-left corner of the theme
    *
    * @param x The x-coordinate to theme this node to
    * @param y The y-coordinate to theme this node to
    */
  override def relocate(x: Double, y: Double): Unit = {
    val clampedX = if (x <~ 0) 0 else x
    val clampedY = if (y <~ 0) 0 else y
    super.relocate(clampedX, clampedY)
    topLeft = (clampedX, clampedY)
  }

  /**
    * Select this theme
    */
  def select(): Unit = {
    selected() = true

    storyViewer.viewer.themesubthemelinkGroups filter (_.endPoints contains this) foreach { grp =>
      grp.links filter (_.from == this) foreach { l => l.select(Color.Red) }
      grp.links filter (_.to == this) foreach { l => l.select(Color.Green) }
    }
    storyViewer.viewer.thememotiflinkGroups filter (_.thetheme == this) foreach { grp =>
      grp.links foreach { l => l.select(Color.Green) }
    }

    pluginEventDispatcher.notifyThemeSelection(id)
  }

  /**
    * Unselect this theme
    */
  def deselect(): Unit = {
    selected() = false
    pluginEventDispatcher.notifyThemeDeselection(id)
  }

  /**
    * Returns the midpoints of the edges of the theme
    */
  def edgePoints = {
    val widthVector = Vector2(width / 2, 0d)
    val heightVector = Vector2(0d, height / 2)

    import ViewerTheme.Edge._
    Map(Left -> (centre - widthVector),
      Right -> (centre + widthVector),
      Top -> (centre - heightVector),
      Bottom -> (centre + heightVector))
  }

  override def toString: String = s"ViewerTheme id: $id, name: ${themeName.getValue}"

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

object ViewerTheme {
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
