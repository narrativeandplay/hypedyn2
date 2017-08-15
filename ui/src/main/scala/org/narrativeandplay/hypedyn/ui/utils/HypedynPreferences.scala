package org.narrativeandplay.hypedyn.ui.utils

import java.io.File
import java.util.prefs.Preferences

import scala.collection.mutable.ArrayBuffer

object HypedynPreferences {
  object Keys {
    val RecentFiles = "recentFiles"
  }

  private val preferences = Preferences.userRoot().node("hypedyn2")
  preferences.flush()

  private val MaxRecentFiles = 10

  def recentFiles: List[File] = {
    val filePaths = preferences get (Keys.RecentFiles, "") split "," filter (_ != "")
    filePaths.toList map (new File(_))
  }

  def recentFiles_=(files: List[File]): Unit = {
    val filePaths = files dropRight (files.size - MaxRecentFiles) map (_.getAbsolutePath)
    preferences.put(Keys.RecentFiles, filePaths.distinct mkString ",")
  }
}
