package org.narrativeandplay.hypedyn.serialisation

/**
 * Represents an element of an AST for serialisation
 */
sealed trait AstElement


/**
 * Base data types
 */

sealed case class AstInteger(i: BigInt) extends AstElement
sealed case class AstFloat(f: Double) extends AstElement
sealed case class AstDecimal(d: BigDecimal) extends AstElement
sealed case class AstString(s: String) extends AstElement
sealed case class AstBoolean(boolean: Boolean) extends AstElement


/**
 * Null types
 */

case object AstNull extends AstElement
case object AstNothing extends AstElement


/**
 * Collection types
 */

sealed case class AstList(elems: AstElement*) extends AstElement{
  /**
   * Converts an `AstList` to a Scala `List`
   */
  def toList = elems.toList
}
sealed case class AstMap(fields: AstField*) extends AstElement {
  /**
   * Retrieves an element from the Map
   *
   * @param key The key of the element
   * @return The value attached to the given key
   */
  @throws[NoSuchElementException]("When no value is available for the given key")
  def apply(key: String): AstElement =
    fields find { case (k, _) => k == key } match {
      case Some((k, v)) => v
      case None => throw new NoSuchElementException(s"No value found for the given key: $key")
    }

  /**
   * Retrieves an element from the Map, in an exception-safe manner. This method will not throw an exception at runtime,
   * instead returning an option containing None if no value is found
   *
   * @param key The key of the element
   * @return An option containing the value attached to the given key, or None is no such value exists
   */
  def get(key: String): Option[AstElement] =
    fields find { case (k, _) => k == key } match {
      case Some((k, v)) => Some(v)
      case None => None
    }

  /**
   * Converts an `AstMap` to a Scala `Map`
   */
  def toMap = fields.toMap
}
