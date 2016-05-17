package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.themes.internal.Theme

/**
  * Change that represents a theme having been updated
  *
  * @param notUpdatedTheme The not updated version of the theme
  * @param updatedTheme The updated version of the theme
  */
sealed case class ThemeUpdatedChange(notUpdatedTheme: Theme, updatedTheme: Theme) extends Undoable {
  /**
    * Defines what to do when an undo action happens
    */
  override def undo(): ThemeUpdatedChange = ThemeUpdatedChange(updatedTheme, notUpdatedTheme)

  /**
    * Defines how to reverse an undo action
    */
  override def redo(): Unit = UndoEventDispatcher.updateTheme(notUpdatedTheme, updatedTheme)
}
