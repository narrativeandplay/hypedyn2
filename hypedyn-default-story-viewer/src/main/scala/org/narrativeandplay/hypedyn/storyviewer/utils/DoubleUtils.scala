package org.narrativeandplay.hypedyn.storyviewer.utils

object DoubleUtils {
  private final val THRESHOLD = 1e-6

  implicit class DoubleWithAlmostEquals(val d: Double) extends AnyVal {
    def ~= (d2: Double) = (d - d2).abs < THRESHOLD

    def !~= (d2: Double) = !(d ~= d2)

    def <~ (d2: Double) = d - d2 < THRESHOLD

    def <~= (d2: Double) = (d ~= d2) || (d <~ d2)

    def >~ (d2: Double) = d - d2 > THRESHOLD

    def >~= (d2: Double) = (d ~= d2) || (d >~ d2)
  }

}
