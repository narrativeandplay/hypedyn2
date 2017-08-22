package org.narrativeandplay.hypedyn.api.utils

import java.lang.{System => Sys}
import java.nio.file.{Path, Paths}

/**
 * OS related utilities
 */
object System {
  private final val Windows = "windows"
  private final val Mac = "mac"
  private final val Linux = "linux"

  private val os = Sys.getProperty("os.name").toLowerCase

  def IsWindows: Boolean = os startsWith Windows
  def IsMac: Boolean = os startsWith Mac
  def IsLinux: Boolean = os startsWith Linux

  def IsUnix: Boolean = !IsWindows

  def DataLocation: Path = {
    if (IsWindows) {
      Paths.get(Sys.getenv("LocalAppData"), "hypedyn")
    }
    else if (IsMac) {
      Paths.get(Sys.getProperty("user.home"), "Library", "Application Support", "hypedyn")
    }
    else {
      Option(Sys.getenv("XDG_DATA_HOME")) match {
        case Some(dataHome) =>
          Paths.get(dataHome, "hypedyn")

        case None =>
          Paths.get(Sys.getProperty("user.home"), ".local", "share", "hypedyn")
      }
    }
  }
}
