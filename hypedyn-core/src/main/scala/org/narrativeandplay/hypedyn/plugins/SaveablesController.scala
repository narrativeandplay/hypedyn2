package org.narrativeandplay.hypedyn.plugins

import org.narrativeandplay.hypedyn.serialisation.SaveHash

object SaveablesController {
  val SaveablePlugins = PluginsController.Plugins collect { case (name, plugin: Saveable) =>
    name -> plugin
  }
  
  def onSave = {
    val data = SaveablePlugins map { case (pluginName, plugin) =>
      pluginName -> plugin.onSave
    }

    SaveHash(data.toSeq: _*)
  }

  def onLoad(data: SaveHash): Unit = {
    SaveablePlugins foreach { case (name, plugin) => plugin.onLoad(data(s"$name")) }
  }
}
