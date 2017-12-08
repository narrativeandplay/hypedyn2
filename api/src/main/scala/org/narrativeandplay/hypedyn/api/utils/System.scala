package org.narrativeandplay.hypedyn.api.utils

import scala.util.Properties

import better.files._

/**
 * OS related utilities
 */
object System {
  private final val Windows = "windows"
  private final val Mac = "mac"
  private final val Linux = "linux"

  private val os = Properties.osName.toLowerCase

  def IsWindows: Boolean = os startsWith Windows
  def IsMac: Boolean = os startsWith Mac
  def IsLinux: Boolean = os startsWith Linux

  def IsUnix: Boolean = !IsWindows

  def DataLocation: File = {
    if (IsWindows) {
      // This env var is guaranteed to exist on Windows, so the else string will never trigger
      Properties.envOrElse("LocalAppData", "") / "HypeDyn 2"
    }
    else if (IsMac) {
      Properties.userHome / "Library" / "Application Support" / "HypeDyn 2"
    }
    else {
      Properties.envOrNone("XDG_DATA_HOME") match {
        case Some(dataHome) =>
          dataHome / "hypedyn2"

        case None =>
          Properties.userHome / ".local" / "share" / "hypedyn2"
      }
    }
  }

  def LogLocation: File = DataLocation / "logs"

  def SettingsFile: File = DataLocation.createChild("hypedyn2.conf")
}
