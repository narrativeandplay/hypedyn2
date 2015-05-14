package org.narrativeandplay.hypedyn.plugins

object SaveablesController {
  val SaveablePlugins = PluginsController.Plugins collect { case (name, plugin: Saveable) =>
    name -> plugin
  }
}
