package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.themes.internal.Theme

sealed abstract class ThemeChange(changedTheme: Theme, t: Theme => Unit) extends Undoable {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): ThemeChange

  /**
    * Defines how to reverse an undo action
    */
  override def redo(): Unit = t(changedTheme)
}

sealed case class ThemeCreatedChange(createdTheme: Theme)
  extends ThemeChange(createdTheme, UndoEventDispatcher.createTheme) {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): ThemeChange = ThemeDestroyedChange(createdTheme)
}

sealed case class ThemeDestroyedChange(destroyedTheme: Theme)
  extends ThemeChange(destroyedTheme, UndoEventDispatcher.destroyTheme) {
  /**
    * Defines the change to produce when an undo action happens
    */
  override def undo(): ThemeChange = ThemeCreatedChange(destroyedTheme)
}