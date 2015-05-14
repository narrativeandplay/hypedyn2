package org.narrativeandplay.hypedyn.plugins

object SaveablesController {
  val SaveablePlugins = PluginsController.plugins collect { case (name, plugin: Saveable) =>
    name -> plugin
  }
}
