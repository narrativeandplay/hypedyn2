package org.narrativeandplay.hypedyn.api.serialisation

/**
 * The typeclass indicating that T is serialisable and has a serialised representation
 *
 * @tparam T The type that is serialisable
 */
trait Serialisable[T] {
  /**
   * Returns the serialised representation of an object
   *
   * @param t The object to serialise
   */
  def serialise(t: T): AstElement

  /**
   * Returns an object given it's serialised representation
   *
   * @param serialised The serialised form of the object
   */
  def deserialise(serialised: AstElement): T
}
