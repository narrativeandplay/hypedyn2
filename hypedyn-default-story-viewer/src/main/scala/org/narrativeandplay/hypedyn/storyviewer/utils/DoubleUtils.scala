package org.narrativeandplay.hypedyn.storyviewer.utils

/**
 * Utilities for doing comparisons on double-precision (64-bit) floating point numbers
 */
object DoubleUtils {
  private final val THRESHOLD = 1e-6

  /**
   * Implicit class to provide the convenience methods to doubles
   * @param d The first double in the comparison
   */
  implicit class DoubleWithAlmostEquals(val d: Double) extends AnyVal {
    /**
     * Compares if 2 doubles are almost equal. Two doubles are almost equal if their difference is
     * less than a threshold value
     *
     * @param d2 The other double to compare to
     * @return True if the doubles are almost equal, false otherwise
     */
    def ~= (d2: Double) = (d - d2).abs < THRESHOLD

    /**
     * Compares if 2 doubles are not almost equal
     *
     * @param d2 The other double to compare to
     * @return True if the doubles are not almost equal, false otherwise
     */
    def !~= (d2: Double) = !(d ~= d2)

    /**
     * Compares if a double is strictly less than another. A double is strictly less than another if
     * it is less than another, and that difference is greater than a threshold value
     *
     * @param d2 The other double to compare to
     * @return True if the first double is strictly less than the second
     */
    def <~ (d2: Double) = d2 - d > THRESHOLD

    /**
     * Compares if a double is strictly less than or is equal to another.
     *
     * @param d2 The double to compare to
     * @return True if the first double is equal to, or is strictly less than the second
     */
    def <~= (d2: Double) = (d ~= d2) || (d <~ d2)

    /**
     * Compares if a double is strictly greater than another. A double is strictly greater than another if
     * it is greater than another, and that difference is greater than a threshold value
     *
     * @param d2 The other double to compare to
     * @return True if the first double is strictly greater than the second
     */
    def >~ (d2: Double) = d - d2 > THRESHOLD

    /**
     * Compares if a double is strictly greater than or is equal to another.
     *
     * @param d2 The double to compare to
     * @return True if the first double is equal to, or is strictly greater than the second
     */
    def >~= (d2: Double) = (d ~= d2) || (d >~ d2)
  }

  def clamp(minD: Double, maxD: Double, d: Double) = minD max d min maxD

}
