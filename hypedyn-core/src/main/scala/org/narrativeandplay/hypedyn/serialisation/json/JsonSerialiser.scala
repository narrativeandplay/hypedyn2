package org.narrativeandplay.hypedyn.serialisation.json

import java.io.{File, PrintWriter}

import org.json4s._
import org.json4s.native.JsonMethods._
import org.narrativeandplay.hypedyn.serialisation._

import scala.io.Source

object JsonSerialiser {
  def save(data: SaveElement, saveFile: File): Unit = {
    val saveData = pretty(render(saveElementToJValue(data)))
    val writer = new PrintWriter(saveFile)

    writer.write(saveData)
    writer.close()
  }

  def load(saveFile: File) = {
    val fileSource = Source.fromFile(saveFile)
    val sourceData = fileSource.mkString
    val parsedData = parse(sourceData)

    fileSource.close()

    jValueToSaveElement(parsedData)
  }

  private def saveElementToJValue(elem: SaveElement): JValue = elem match {
    case SaveInt(i) => JInt(i)
    case SaveFloat(f) => JDouble(f)
    case SaveString(s) => JString(s)
    case SaveList(ls @ _*) => JArray(ls.map(saveElementToJValue _).toList)
    case SaveHash(hs @ _*) => JObject(hs.map { case (k, v) =>
      k -> saveElementToJValue(v)
    }.toList)
  }

  private def jValueToSaveElement(jValue: JValue): SaveElement = jValue match {
    case JInt(i) => SaveInt(i.toLong)
    case JDouble(f) => SaveFloat(f)
    case JString(s) => SaveString(s)
    case JArray(a) => SaveList(a.map(jValueToSaveElement _).toSeq: _*)
    case JObject(o) => SaveHash(o.map { case (k, v) =>
      k -> jValueToSaveElement(v)
    }.toSeq: _*)
  }
}
