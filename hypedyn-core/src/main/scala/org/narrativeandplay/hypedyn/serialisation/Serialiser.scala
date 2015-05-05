package org.narrativeandplay.hypedyn.serialisation

object Serialiser {
  def serialise(data: SaveHash): String = json.JsonSerialiser serialise data
  def deserialise(data: String) = json.JsonSerialiser deserialise data
}
