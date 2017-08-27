package org.narrativeandplay.hypedyn.ui.utils

import better.files._
import pureconfig.ConvertHelpers._
import pureconfig._

import org.narrativeandplay.hypedyn.api.logging.Logger
import org.narrativeandplay.hypedyn.api.utils.System

object Settings {
  private[this] case class Settings(recentFiles: List[File] = Nil)

  private implicit val fileConverter: ConfigConvert[File] = ConfigConvert.viaString[File](
    catchReadError(File(_)),
    _.toString()
  )

  private val settings = loadConfig[Settings](System.SettingsFile.path) match {
    case Left(errs) =>
      Logger.error("Error(s) occurred when attempting to read settings:")
      errs.toList foreach { err => Logger.error(err) }
      Settings()

    case Right(v) =>
      v
  }

  private val MaxRecentFiles = 10

  def recentFiles: List[File] = settings.recentFiles

  def recentFiles_=(files: List[File]): Unit = {
    val newSettings = Settings(files.distinct dropRight (files.size - MaxRecentFiles))

    saveConfigAsPropertyFile(newSettings, System.SettingsFile.path, overrideOutputPath = true)
  }
}
