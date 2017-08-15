package org.narrativeandplay.hypedyn.core.plugins

import java.io.File
import java.net.{URL, URLClassLoader}

import scala.collection.mutable

import org.clapper.classutil.ClassFinder

import org.narrativeandplay.hypedyn.api.plugins.Plugin

/**
 * Controller that handles plugin instantiation
 */
object PluginsController {
  private val PluginClassName = classOf[Plugin].getCanonicalName

  private final val PluginFolder = new File("plugins")

  private val _plugins = mutable.HashMap.empty[String, Plugin]

  /**
   * Returns the list of plugins as a Map
   */
  def plugins = _plugins.toMap

  /**
   * Initialisation function to find and instantiate plugins
   */
  private def init(): Unit = {
    val pluginFolderFiles = Option(PluginFolder.listFiles()) getOrElse Array.empty[File]
    val pluginJars = pluginFolderFiles filter (_.getName.endsWith(".jar"))
    val classpath = ClassFinder.classpath ++ pluginJars
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses()
    val classMap = ClassFinder classInfoMap classes.iterator
    val pluginsToLoad = ClassFinder concreteSubclasses (PluginClassName, classMap)

    val loader = new URLClassLoader(pluginJars map { f => new URL(s"file:${f.getAbsolutePath}") }, ClassLoader.getSystemClassLoader)

    pluginsToLoad.map(_.name).foreach { pluginClassName =>
      val plugin = loader.loadClass(pluginClassName).newInstance().asInstanceOf[Plugin]

      _plugins += plugin.name -> plugin
    }
  }

  init()
}
