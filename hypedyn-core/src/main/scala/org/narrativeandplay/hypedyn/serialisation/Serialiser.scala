package org.narrativeandplay.hypedyn.serialisation

import serialisers._

object Serialiser {
  /**
   * Object <-> Ast Serialisers
   */

  /**
   *
   * @param t
   * @param serialiser
   * @tparam T
   * @return
   */
  def serialise[T](t: T)(implicit serialiser: Serialisable[T]) = serialiser.serialise(t)
  def deserialise[T](data: AstElement)(implicit serialiser: Serialisable[T]) = serialiser.deserialise(data)

  /**
   * Ast <-> String Serialisers
   */

  def toString(data: AstElement) = JsonSerialiser serialise data
  def fromString(string: String) = JsonSerialiser deserialise string
}
