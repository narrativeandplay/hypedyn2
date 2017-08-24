package org.narrativeandplay.hypedyn.ui.utils

import java.util.prefs.Preferences

import better.files._

object HypedynPreferences {
  object Keys {
    val RecentFiles = "recentFiles"
  }

  private val preferences = Preferences.userRoot().node("hypedyn2")
  preferences.flush()

  private val MaxRecentFiles = 10

  def recentFiles: List[File] = {
    val filePaths = preferences get (Keys.RecentFiles, "") split "," filter (_ != "")
    filePaths.toList map (File(_))
  }

  def recentFiles_=(files: List[File]): Unit = {
    val filePaths = files dropRight (files.size - MaxRecentFiles) map (_.toString())
    preferences.put(Keys.RecentFiles, filePaths.distinct mkString ",")
  }
}
