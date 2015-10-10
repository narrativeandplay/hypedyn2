package org.narrativeandplay.hypedyn.dialogs

import java.io.File

import scalafx.stage.{Window, DirectoryChooser}

/**
 * A wrapper over the ScalaFX directory chooser, for correcting the ScalaFX API
 *
 * @param ownerWindow The parent dialog of the directory chooser, for inheriting icons
 */
class DirectoryDialog(ownerWindow: Window) extends DirectoryChooser {

  /**
   * Shows a new directory selection dialog.
   *
   * @return An option containing the selected directory, or None if no directory was selected
   */
  def showDialog(): Option[File] = Option(super.showDialog(ownerWindow))
}
