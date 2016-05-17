package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.story.themes.ThematicElementID
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer

/**
  * Change representing a theme having been moved
  *
  * @param eventHandler The event dispatcher allowed to send events
  * @param themeId The ID of the theme moved
  * @param initialPos The initial position of the theme
  * @param finalPos The final position of the theme
  */
case class ThemeMovedChange(eventHandler: StoryViewer,
                            themeId: ThematicElementID,
                            initialPos: Vector2[Double],
                            finalPos: Vector2[Double]) extends Undoable {
  override def undo(): ThemeMovedChange = ThemeMovedChange(eventHandler, themeId, finalPos, initialPos)

  override def redo(): Unit = {
    eventHandler.moveTheme(themeId, finalPos)
    eventHandler.notifyThemeMove(themeId, initialPos, finalPos)
  }

  override def merge(other: Undoable): Option[Undoable] = other match {
    case c: ThemeMovedChange =>
      if (themeId == c.themeId) Some(new ThemeMovedChange(eventHandler, themeId, initialPos, c.finalPos)) else None
    case _ => None
  }
}