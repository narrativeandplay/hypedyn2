package org.narrativeandplay.hypedyn.dialogs

import java.io.File

import scalafx.Includes._
import scalafx.stage.{Window, FileChooser}
import scalafx.stage.FileChooser.ExtensionFilter

class FileDialog(ownerWindow: Window) extends FileChooser {
  extensionFilters += new ExtensionFilter("HypeDyn Story", "*.dyn")

  def showOpenFileDialog(): Option[File] = Option(super.showOpenDialog(ownerWindow))

  def showSaveFileDialog(): Option[File] = Option(super.showSaveDialog(ownerWindow))
}
