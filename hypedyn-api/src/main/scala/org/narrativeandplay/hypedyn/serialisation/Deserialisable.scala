package org.narrativeandplay.hypedyn.serialisation

/**
 * The typeclass indicating that T is deserialisable
 *
 * @tparam T The type that is serialisable
 */
trait Deserialisable[T] {
  /**
   * Returns an object given it's serialised representation
   *
   * @param serialised The serialised form of the object
   */
  def deserialise(serialised: AstElement): T
}
