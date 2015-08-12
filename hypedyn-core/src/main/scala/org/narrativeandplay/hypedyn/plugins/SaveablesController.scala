package org.narrativeandplay.hypedyn.plugins

/**
 * Controller for plugins implementing the Saveable interface
 */
object SaveablesController {
  /**
   * The list of plugins implementing Saveable
   */
  val SaveablePlugins = PluginsController.plugins collect { case (name, plugin: Saveable) =>
    name -> plugin
  }
}
