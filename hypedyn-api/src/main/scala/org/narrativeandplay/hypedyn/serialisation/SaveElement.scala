package org.narrativeandplay.hypedyn.serialisation

/**
 * Base trait for all save data objects
 */
sealed trait SaveElement

sealed case class SaveInt(i: Long) extends SaveElement

sealed case class SaveFloat(f: Double) extends SaveElement

sealed case class SaveString(s: String) extends SaveElement

sealed case class SaveBoolean(b: Boolean) extends SaveElement

sealed case class SaveDecimal(d: BigDecimal) extends SaveElement

case object SaveNothing extends SaveElement

case object SaveNull extends SaveElement

sealed case class SaveList(elems: SaveElement*) extends SaveElement

sealed case class SaveHash(fields: SaveField*) extends SaveElement {
  def apply(key: String) = {
    fields.find { case (k, _) => k == key } match {
      case Some((k, v)) => v
      case None => throw new NoSuchElementException("No value found for the given key")
    }
  }
}
