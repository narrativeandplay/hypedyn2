package org.narrativeandplay.hypedyn.serialisation

import serialisers._

object Serialiser {
  /**
   * Object <-> Ast Serialisers
   */

  /**
   * Serialises a serialisable object
   *
   * @param t The object to serialise
   * @param serialiser The typeclass instance implementing Serialisable for the type T
   * @tparam T The type of the object to serialise
   * @return The serialised form of the object
   */
  def serialise[T](t: T)(implicit serialiser: Serialisable[T]) = serialiser.serialise(t)

  /**
   * Deserialises a serialisable object
   *
   * @param data The serialised data to deserialise
   * @param deserialiser The typeclass instance implementing Serialisable for the type T
   * @tparam T The type of the object to deserialise
   * @return The deserialised object
   */
  def deserialise[T](data: AstElement)(implicit deserialiser: Deserialisable[T]) = deserialiser.deserialise(data)

  /**
   * Ast <-> String Serialisers
   */

  /**
   * Transforms serialised data to a string
   *
   * @param data The data to transform
   * @return The string form of the data
   */
  def toString(data: AstElement) = JsonSerialiser serialise data

  /**
   * Transforms a string into serialised data
   *
   * @param string The string to transform
   * @return The serialised data form of the string
   */
  def fromString(string: String) = JsonSerialiser deserialise string
}
