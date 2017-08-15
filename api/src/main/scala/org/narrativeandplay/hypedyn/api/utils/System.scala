package org.narrativeandplay.hypedyn.api.utils

import java.lang.{System => Sys}

/**
 * OS related utilities
 */
object System {
  private final val Windows = "windows"
  private final val Mac = "mac"
  private final val Linux = "linux"

  private val os = Sys.getProperty("os.name").toLowerCase

  def isWindows = os startsWith Windows
  def isMac = os startsWith Mac
  def isLinux = os startsWith Linux

  def isUnix = !isWindows
}
