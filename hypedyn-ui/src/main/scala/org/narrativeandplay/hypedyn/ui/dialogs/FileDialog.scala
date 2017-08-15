package org.narrativeandplay.hypedyn.ui.dialogs

import java.io.File

import scalafx.Includes._
import scalafx.stage.{Window, FileChooser}
import scalafx.stage.FileChooser.ExtensionFilter

/**
 * A wrapper over the ScalaFX file chooser, for correcting the ScalaFX API
 *
 * @param ownerWindow The parent dialog of the file chooser, for inheriting icons
 */
class FileDialog(ownerWindow: Window) extends FileChooser {
  extensionFilters += new ExtensionFilter("HypeDyn 2 Story", "*.dyn2")

  /**
   * Shows a new open file dialog
   *
   * @return An option containing the selected file, or None if no file was selected
   */
  def showOpenFileDialog(): Option[File] = Option(super.showOpenDialog(ownerWindow))

  /**
   * Shows a new save file dialog
   *
   * @return An option containing the selected file, or None if no file was selected
   */
  def showSaveFileDialog(): Option[File] = Option(super.showSaveDialog(ownerWindow))
}
