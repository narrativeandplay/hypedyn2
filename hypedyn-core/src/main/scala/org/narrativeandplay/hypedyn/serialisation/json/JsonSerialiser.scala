package org.narrativeandplay.hypedyn.serialisation.json

import org.json4s._
import org.json4s.native.JsonMethods._
import org.narrativeandplay.hypedyn.serialisation._

object JsonSerialiser {
  def serialise(data: SaveElement) = pretty(render(saveElementToJValue(data)))
  def deserialise(data: String): SaveElement = jValueToSaveElement(parse(data))

  private def saveElementToJValue(elem: SaveElement): JValue = elem match {
    case SaveInt(i) => JInt(i)
    case SaveFloat(f) => JDouble(f)
    case SaveString(s) => JString(s)
    case SaveBoolean(b) => JBool(b)
    case SaveDecimal(d) => JDecimal(d)
    case SaveNothing => JNothing
    case SaveNull => JNull
    case SaveList(ls @ _*) => JArray((ls map saveElementToJValue).toList)
    case SaveHash(hs @ _*) => JObject(hs.map { case (k, v) =>
      k -> saveElementToJValue(v)
    }.toList)
  }

  private def jValueToSaveElement(jValue: JValue): SaveElement = jValue match {
    case JInt(i) => SaveInt(i.toLong)
    case JDouble(f) => SaveFloat(f)
    case JString(s) => SaveString(s)
    case JBool(b) => SaveBoolean(b)
    case JDecimal(d) => SaveDecimal(d)
    case JNothing => SaveNothing
    case JNull => SaveNull
    case JArray(a) => SaveList((a map jValueToSaveElement).toSeq: _*)
    case JObject(o) => SaveHash(o.map { case (k, v) =>
      k -> jValueToSaveElement(v)
    }.toSeq: _*)
  }
}
