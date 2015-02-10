package org.narrativeandplay.hypedyn.plugins.serialisation

import java.io.File

import org.narrativeandplay.hypedyn.plugins.serialisation.json.JsonSerialiser

object Serialiser {
  def save(file: File): Unit = {
//    val saveable = SaveHash("plugins" -> PluginManager.save)
//    JsonSerialiser.save(saveable, file)
  }

  def load(file: File) = {
    val data = JsonSerialiser.load(file).asInstanceOf[SaveHash]

//    PluginManager.load(data("plugins").asInstanceOf[SaveHash])
  }
}
