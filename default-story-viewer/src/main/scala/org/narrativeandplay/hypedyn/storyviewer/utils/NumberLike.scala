/**
 * MIT License
 *
 * Copyright (c) 2014-2017 Benedict Lee
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

// Code taken from https://github.com/benedictleejh/scala-math-vector

package org.narrativeandplay.hypedyn.storyviewer.utils

trait NumberLike[A, B, C] {
  def plus(x: A, y: B): C
  def minus(x: A, y: B): C
  def times(x: A, y: B): C
  def divide(x: A, y: B): C
}

// Scala numeric types: Byte, Short, Int, Long, Float, Double, BigInt, BigDecimal
object NumberLike {

  /** ******************
   * Byte Operations
   *
   * ******************/
  implicit object NumberLikeByteByte extends NumberLike[Byte, Byte, Int] {
    def plus(x: Byte, y: Byte): Int = x + y
    def divide(x: Byte, y: Byte): Int = x / y
    def times(x: Byte, y: Byte): Int = x * y
    def minus(x: Byte, y: Byte): Int = x - y
  }

  implicit object NumberLikeByteShort extends NumberLike[Byte, Short, Int] {
    def plus(x: Byte, y: Short): Int = x + y
    def divide(x: Byte, y: Short): Int = x / y
    def times(x: Byte, y: Short): Int = x * y
    def minus(x: Byte, y: Short): Int = x - y
  }

  implicit object NumberLikeByteInt extends NumberLike[Byte, Int, Int] {
    def plus(x: Byte, y: Int): Int = x + y
    def divide(x: Byte, y: Int): Int = x / y
    def times(x: Byte, y: Int): Int = x * y
    def minus(x: Byte, y: Int): Int = x - y
  }

  implicit object NumberLikeByteLong extends NumberLike[Byte, Long, Long] {
    def plus(x: Byte, y: Long): Long = x + y
    def divide(x: Byte, y: Long): Long = x / y
    def times(x: Byte, y: Long): Long = x * y
    def minus(x: Byte, y: Long): Long = x - y
  }

  implicit object NumberLikeByteFloat extends NumberLike[Byte, Float, Float] {
    def plus(x: Byte, y: Float): Float = x + y
    def divide(x: Byte, y: Float): Float = x / y
    def times(x: Byte, y: Float): Float = x * y
    def minus(x: Byte, y: Float): Float = x - y
  }

  implicit object NumberLikeByteDouble extends NumberLike[Byte, Double, Double] {
    def plus(x: Byte, y: Double): Double = x + y
    def divide(x: Byte, y: Double): Double = x / y
    def times(x: Byte, y: Double): Double = x * y
    def minus(x: Byte, y: Double): Double = x - y
  }

  implicit object NumberLikeByteBigInt extends NumberLike[Byte, BigInt, BigInt] {
    def plus(x: Byte, y: BigInt): BigInt = x + y
    def divide(x: Byte, y: BigInt): BigInt = x / y
    def times(x: Byte, y: BigInt): BigInt = x * y
    def minus(x: Byte, y: BigInt): BigInt = x - y
  }

  implicit object NumberLikeByteBigDecimal extends NumberLike[Byte, BigDecimal, BigDecimal] {
    def plus(x: Byte, y: BigDecimal): BigDecimal = x + y
    def divide(x: Byte, y: BigDecimal): BigDecimal = x / y
    def times(x: Byte, y: BigDecimal): BigDecimal = x * y
    def minus(x: Byte, y: BigDecimal): BigDecimal = x - y
  }

  /** ******************
   * Short Operations
   *
   * ******************/
  implicit object NumberLikeShortByte extends NumberLike[Short, Byte, Int] {
    def plus(x: Short, y: Byte): Int = x + y
    def divide(x: Short, y: Byte): Int = x / y
    def times(x: Short, y: Byte): Int = x * y
    def minus(x: Short, y: Byte): Int = x - y
  }

  implicit object NumberLikeShortShort extends NumberLike[Short, Short, Int] {
    def plus(x: Short, y: Short): Int = x + y
    def divide(x: Short, y: Short): Int = x / y
    def times(x: Short, y: Short): Int = x * y
    def minus(x: Short, y: Short): Int = x - y
  }

  implicit object NumberLikeShortInt extends NumberLike[Short, Int, Int] {
    def plus(x: Short, y: Int): Int = x + y
    def divide(x: Short, y: Int): Int = x / y
    def times(x: Short, y: Int): Int = x * y
    def minus(x: Short, y: Int): Int = x - y
  }

  implicit object NumberLikeShortLong extends NumberLike[Short, Long, Long] {
    def plus(x: Short, y: Long): Long = x + y
    def divide(x: Short, y: Long): Long = x / y
    def times(x: Short, y: Long): Long = x * y
    def minus(x: Short, y: Long): Long = x - y
  }

  implicit object NumberLikeShortFloat extends NumberLike[Short, Float, Float] {
    def plus(x: Short, y: Float): Float = x + y
    def divide(x: Short, y: Float): Float = x / y
    def times(x: Short, y: Float): Float = x * y
    def minus(x: Short, y: Float): Float = x - y
  }

  implicit object NumberLikeShortDouble extends NumberLike[Short, Double, Double] {
    def plus(x: Short, y: Double): Double = x + y
    def divide(x: Short, y: Double): Double = x / y
    def times(x: Short, y: Double): Double = x * y
    def minus(x: Short, y: Double): Double = x - y
  }

  implicit object NumberLikeShortBigInt extends NumberLike[Short, BigInt, BigInt] {
    def plus(x: Short, y: BigInt): BigInt = x + y
    def divide(x: Short, y: BigInt): BigInt = x / y
    def times(x: Short, y: BigInt): BigInt = x * y
    def minus(x: Short, y: BigInt): BigInt = x - y
  }

  implicit object NumberLikeShortBigDecimal extends NumberLike[Short, BigDecimal, BigDecimal] {
    def plus(x: Short, y: BigDecimal): BigDecimal = x + y
    def divide(x: Short, y: BigDecimal): BigDecimal = x / y
    def times(x: Short, y: BigDecimal): BigDecimal = x * y
    def minus(x: Short, y: BigDecimal): BigDecimal = x - y
  }

  /** ****************
   * Int Operations
   *
   * ****************/
  implicit object NumberLikeIntByte extends NumberLike[Int, Byte, Int] {
    def plus(x: Int, y: Byte): Int = x + y
    def divide(x: Int, y: Byte): Int = x / y
    def times(x: Int, y: Byte): Int = x * y
    def minus(x: Int, y: Byte): Int = x - y
  }

  implicit object NumberLikeIntShort extends NumberLike[Int, Short, Int] {
    def plus(x: Int, y: Short): Int = x + y
    def divide(x: Int, y: Short): Int = x / y
    def times(x: Int, y: Short): Int = x * y
    def minus(x: Int, y: Short): Int = x - y
  }

  implicit object NumberLikeIntInt extends NumberLike[Int, Int, Int] {
    def plus(x: Int, y: Int): Int = x + y
    def divide(x: Int, y: Int): Int = x / y
    def times(x: Int, y: Int): Int = x * y
    def minus(x: Int, y: Int): Int = x - y
  }

  implicit object NumberLikeIntLong extends NumberLike[Int, Long, Long] {
    def plus(x: Int, y: Long): Long = x + y
    def divide(x: Int, y: Long): Long = x / y
    def times(x: Int, y: Long): Long = x * y
    def minus(x: Int, y: Long): Long = x - y
  }

  implicit object NumberLikeIntFloat extends NumberLike[Int, Float, Float] {
    def plus(x: Int, y: Float): Float = x + y
    def divide(x: Int, y: Float): Float = x / y
    def times(x: Int, y: Float): Float = x * y
    def minus(x: Int, y: Float): Float = x - y
  }

  implicit object NumberLikeIntDouble extends NumberLike[Int, Double, Double] {
    def plus(x: Int, y: Double): Double = x + y
    def divide(x: Int, y: Double): Double = x / y
    def times(x: Int, y: Double): Double = x * y
    def minus(x: Int, y: Double): Double = x - y
  }

  implicit object NumberLikeIntBigInt extends NumberLike[Int, BigInt, BigInt] {
    def plus(x: Int, y: BigInt): BigInt = x + y
    def divide(x: Int, y: BigInt): BigInt = x / y
    def times(x: Int, y: BigInt): BigInt = x * y
    def minus(x: Int, y: BigInt): BigInt = x - y
  }

  implicit object NumberLikeIntBigDecimal extends NumberLike[Int, BigDecimal, BigDecimal] {
    def plus(x: Int, y: BigDecimal): BigDecimal = x + y
    def divide(x: Int, y: BigDecimal): BigDecimal = x / y
    def times(x: Int, y: BigDecimal): BigDecimal = x * y
    def minus(x: Int, y: BigDecimal): BigDecimal = x - y
  }

  /** ********************
   * Long Operations
   *
   * ********************/
  implicit object NumberLikeLongByte extends NumberLike[Long, Byte, Long] {
    def plus(x: Long, y: Byte): Long = x + y
    def divide(x: Long, y: Byte): Long = x / y
    def times(x: Long, y: Byte): Long = x * y
    def minus(x: Long, y: Byte): Long = x - y
  }

  implicit object NumberLikeLongShort extends NumberLike[Long, Short, Long] {
    def plus(x: Long, y: Short): Long = x + y
    def divide(x: Long, y: Short): Long = x / y
    def times(x: Long, y: Short): Long = x * y
    def minus(x: Long, y: Short): Long = x - y
  }

  implicit object NumberLikeLongInt extends NumberLike[Long, Int, Long] {
    def plus(x: Long, y: Int): Long = x + y
    def divide(x: Long, y: Int): Long = x / y
    def times(x: Long, y: Int): Long = x * y
    def minus(x: Long, y: Int): Long = x - y
  }

  implicit object NumberLikeLongLong extends NumberLike[Long, Long, Long] {
    def plus(x: Long, y: Long): Long = x + y
    def divide(x: Long, y: Long): Long = x / y
    def times(x: Long, y: Long): Long = x * y
    def minus(x: Long, y: Long): Long = x - y
  }

  implicit object NumberLikeLongFloat extends NumberLike[Long, Float, Float] {
    def plus(x: Long, y: Float): Float = x + y
    def divide(x: Long, y: Float): Float = x / y
    def times(x: Long, y: Float): Float = x * y
    def minus(x: Long, y: Float): Float = x - y
  }

  implicit object NumberLikeLongDouble extends NumberLike[Long, Double, Double] {
    def plus(x: Long, y: Double): Double = x + y
    def divide(x: Long, y: Double): Double = x / y
    def times(x: Long, y: Double): Double = x * y
    def minus(x: Long, y: Double): Double = x - y
  }

  implicit object NumberLikeLongBigInt extends NumberLike[Long, BigInt, BigInt] {
    def plus(x: Long, y: BigInt): BigInt = x + y
    def divide(x: Long, y: BigInt): BigInt = x / y
    def times(x: Long, y: BigInt): BigInt = x * y
    def minus(x: Long, y: BigInt): BigInt = x - y
  }

  implicit object NumberLikeLongBigDecimal extends NumberLike[Long, BigDecimal, BigDecimal] {
    def plus(x: Long, y: BigDecimal): BigDecimal = x + y
    def divide(x: Long, y: BigDecimal): BigDecimal = x / y
    def times(x: Long, y: BigDecimal): BigDecimal = x * y
    def minus(x: Long, y: BigDecimal): BigDecimal = x - y
  }

  /** *******************
   * Float Operations
   *
   * *******************/
  implicit object NumberLikeFloatByte extends NumberLike[Float, Byte, Float] {
    def plus(x: Float, y: Byte): Float = x + y
    def divide(x: Float, y: Byte): Float = x / y
    def times(x: Float, y: Byte): Float = x * y
    def minus(x: Float, y: Byte): Float = x - y
  }

  implicit object NumberLikeFloatShort extends NumberLike[Float, Short, Float] {
    def plus(x: Float, y: Short): Float = x + y
    def divide(x: Float, y: Short): Float = x / y
    def times(x: Float, y: Short): Float = x * y
    def minus(x: Float, y: Short): Float = x - y
  }

  implicit object NumberLikeFloatInt extends NumberLike[Float, Int, Float] {
    def plus(x: Float, y: Int): Float = x + y
    def divide(x: Float, y: Int): Float = x / y
    def times(x: Float, y: Int): Float = x * y
    def minus(x: Float, y: Int): Float = x - y
  }

  implicit object NumberLikeFloatLong extends NumberLike[Float, Long, Float] {
    def plus(x: Float, y: Long): Float = x + y
    def divide(x: Float, y: Long): Float = x / y
    def times(x: Float, y: Long): Float = x * y
    def minus(x: Float, y: Long): Float = x - y
  }

  implicit object NumberLikeFloatFloat extends NumberLike[Float, Float, Float] {
    def plus(x: Float, y: Float): Float = x + y
    def divide(x: Float, y: Float): Float = x / y
    def times(x: Float, y: Float): Float = x * y
    def minus(x: Float, y: Float): Float = x - y
  }

  implicit object NumberLikeFloatDouble extends NumberLike[Float, Double, Double] {
    def plus(x: Float, y: Double): Double = x + y
    def divide(x: Float, y: Double): Double = x / y
    def times(x: Float, y: Double): Double = x * y
    def minus(x: Float, y: Double): Double = x - y
  }

  implicit object NumberLikeFloatBigDecimal extends NumberLike[Float, BigDecimal, BigDecimal] {
    def plus(x: Float, y: BigDecimal): BigDecimal = x + y
    def divide(x: Float, y: BigDecimal): BigDecimal = x / y
    def times(x: Float, y: BigDecimal): BigDecimal = x * y
    def minus(x: Float, y: BigDecimal): BigDecimal = x - y
  }

  /** **********************
   * Double Operations
   *
   * **********************/
  implicit object NumberLikeDoubleByte extends NumberLike[Double, Byte, Double] {
    def plus(x: Double, y: Byte): Double = x + y
    def divide(x: Double, y: Byte): Double = x / y
    def times(x: Double, y: Byte): Double = x * y
    def minus(x: Double, y: Byte): Double = x - y
  }

  implicit object NumberLikeDoubleShort extends NumberLike[Double, Short, Double] {
    def plus(x: Double, y: Short): Double = x + y
    def divide(x: Double, y: Short): Double = x / y
    def times(x: Double, y: Short): Double = x * y
    def minus(x: Double, y: Short): Double = x - y
  }

  implicit object NumberLikeDoubleInt extends NumberLike[Double, Int, Double] {
    def plus(x: Double, y: Int): Double = x + y
    def divide(x: Double, y: Int): Double = x / y
    def times(x: Double, y: Int): Double = x * y
    def minus(x: Double, y: Int): Double = x - y
  }

  implicit object NumberLikeDoubleLong extends NumberLike[Double, Long, Double] {
    def plus(x: Double, y: Long): Double = x + y
    def divide(x: Double, y: Long): Double = x / y
    def times(x: Double, y: Long): Double = x * y
    def minus(x: Double, y: Long): Double = x - y
  }

  implicit object NumberLikeDoubleFloat extends NumberLike[Double, Float, Double] {
    def plus(x: Double, y: Float): Double = x + y
    def divide(x: Double, y: Float): Double = x / y
    def times(x: Double, y: Float): Double = x * y
    def minus(x: Double, y: Float): Double = x - y
  }

  implicit object NumberLikeDoubleDouble extends NumberLike[Double, Double, Double] {
    def plus(x: Double, y: Double): Double = x + y
    def divide(x: Double, y: Double): Double = x / y
    def times(x: Double, y: Double): Double = x * y
    def minus(x: Double, y: Double): Double = x - y
  }

  implicit object NumberLikeDoubleBigDecimal extends NumberLike[Double, BigDecimal, BigDecimal] {
    def plus(x: Double, y: BigDecimal): BigDecimal = x + y
    def divide(x: Double, y: BigDecimal): BigDecimal = x / y
    def times(x: Double, y: BigDecimal): BigDecimal = x * y
    def minus(x: Double, y: BigDecimal): BigDecimal = x - y
  }

  /** **********************
   * BigInt Operations
   *
   * **********************/
  implicit object NumberLikeBigIntByte extends NumberLike[BigInt, Byte, BigInt] {
    def plus(x: BigInt, y: Byte): BigInt = x + y
    def divide(x: BigInt, y: Byte): BigInt = x / y
    def times(x: BigInt, y: Byte): BigInt = x * y
    def minus(x: BigInt, y: Byte): BigInt = x - y
  }

  implicit object NumberLikeBigIntShort extends NumberLike[BigInt, Short, BigInt] {
    def plus(x: BigInt, y: Short): BigInt = x + y
    def divide(x: BigInt, y: Short): BigInt = x / y
    def times(x: BigInt, y: Short): BigInt = x * y
    def minus(x: BigInt, y: Short): BigInt = x - y
  }

  implicit object NumberLikeBigIntInt extends NumberLike[BigInt, Int, BigInt] {
    def plus(x: BigInt, y: Int): BigInt = x + y
    def divide(x: BigInt, y: Int): BigInt = x / y
    def times(x: BigInt, y: Int): BigInt = x * y
    def minus(x: BigInt, y: Int): BigInt = x - y
  }

  implicit object NumberLikeBigIntLong extends NumberLike[BigInt, Long, BigInt] {
    def plus(x: BigInt, y: Long): BigInt = x + y
    def divide(x: BigInt, y: Long): BigInt = x / y
    def times(x: BigInt, y: Long): BigInt = x * y
    def minus(x: BigInt, y: Long): BigInt = x - y
  }

  implicit object NumberLikeBigIntBigInt extends NumberLike[BigInt, BigInt, BigInt] {
    def plus(x: BigInt, y: BigInt): BigInt = x + y
    def divide(x: BigInt, y: BigInt): BigInt = x / y
    def times(x: BigInt, y: BigInt): BigInt = x * y
    def minus(x: BigInt, y: BigInt): BigInt = x - y
  }

  /** ************************
   * BigDecimal Operations
   *
   * ************************/
  implicit object NumberLikeBigDecimalByte extends NumberLike[BigDecimal, Byte, BigDecimal] {
    def plus(x: BigDecimal, y: Byte): BigDecimal = x + y
    def divide(x: BigDecimal, y: Byte): BigDecimal = x / y
    def times(x: BigDecimal, y: Byte): BigDecimal = x * y
    def minus(x: BigDecimal, y: Byte): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalShort extends NumberLike[BigDecimal, Short, BigDecimal] {
    def plus(x: BigDecimal, y: Short): BigDecimal = x + y
    def divide(x: BigDecimal, y: Short): BigDecimal = x / y
    def times(x: BigDecimal, y: Short): BigDecimal = x * y
    def minus(x: BigDecimal, y: Short): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalInt extends NumberLike[BigDecimal, Int, BigDecimal] {
    def plus(x: BigDecimal, y: Int): BigDecimal = x + y
    def divide(x: BigDecimal, y: Int): BigDecimal = x / y
    def times(x: BigDecimal, y: Int): BigDecimal = x * y
    def minus(x: BigDecimal, y: Int): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalLong extends NumberLike[BigDecimal, Long, BigDecimal] {
    def plus(x: BigDecimal, y: Long): BigDecimal = x + y
    def divide(x: BigDecimal, y: Long): BigDecimal = x / y
    def times(x: BigDecimal, y: Long): BigDecimal = x * y
    def minus(x: BigDecimal, y: Long): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalFloat extends NumberLike[BigDecimal, Float, BigDecimal] {
    def plus(x: BigDecimal, y: Float): BigDecimal = x + y
    def divide(x: BigDecimal, y: Float): BigDecimal = x / y
    def times(x: BigDecimal, y: Float): BigDecimal = x * y
    def minus(x: BigDecimal, y: Float): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalDouble extends NumberLike[BigDecimal, Double, BigDecimal] {
    def plus(x: BigDecimal, y: Double): BigDecimal = x + y
    def divide(x: BigDecimal, y: Double): BigDecimal = x / y
    def times(x: BigDecimal, y: Double): BigDecimal = x * y
    def minus(x: BigDecimal, y: Double): BigDecimal = x - y
  }

  implicit object NumberLikeBigDecimalBigDecimal extends NumberLike[BigDecimal, BigDecimal, BigDecimal] {
    def plus(x: BigDecimal, y: BigDecimal): BigDecimal = x + y
    def divide(x: BigDecimal, y: BigDecimal): BigDecimal = x / y
    def times(x: BigDecimal, y: BigDecimal): BigDecimal = x * y
    def minus(x: BigDecimal, y: BigDecimal): BigDecimal = x - y
  }
}
