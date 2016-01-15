package org.narrativeandplay.hypedyn.logging

object Logger {
  private val logger = grizzled.slf4j.Logger[this.type]

  def info(msg: => Any) = logger.info(msg)
  def info(msg: => Any, t: => Throwable) = logger.info(msg, t)

  def debug(msg: => Any) = logger.debug(msg)
  def debug(msg: => Any, t: => Throwable) = logger.debug(msg, t)

  def error(msg: => Any) = logger.error(msg)
  def error(msg: => Any, t: => Throwable) = logger.error(msg, t)
}
