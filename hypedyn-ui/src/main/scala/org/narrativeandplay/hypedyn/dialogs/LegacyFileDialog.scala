package org.narrativeandplay.hypedyn.dialogs

import java.io.File

import scalafx.Includes._
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{FileChooser, Window}

/**
 * A wrapper over the ScalaFX file chooser, for correcting the ScalaFX API
 *
 * @param ownerWindow The parent dialog of the file chooser, for inheriting icons
 */
class LegacyFileDialog(ownerWindow: Window) extends FileChooser {
  extensionFilters += new ExtensionFilter("HypeDyn 1 Story", "*.dyn")

  /**
   * Shows a new open file dialog
   *
   * @return An option containing the selected file, or None if no file was selected
   */
  def showOpenFileDialog(): Option[File] = Option(super.showOpenDialog(ownerWindow))
}
