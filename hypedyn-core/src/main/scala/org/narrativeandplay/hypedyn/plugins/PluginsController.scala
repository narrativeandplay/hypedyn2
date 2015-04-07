package org.narrativeandplay.hypedyn.plugins

import java.io.File
import java.net.{URL, URLClassLoader}

import org.clapper.classutil.ClassFinder

import scala.collection.mutable

object PluginsController {
  /**
   * The fully qualified name of the the class/trait of the plugin to be loaded
   */
  val PluginClassName = classOf[Plugin].getCanonicalName

  private final val PluginFolder = new File("plugins")

  val Plugins = mutable.HashMap.empty[String, Plugin]

  def init(): Unit = {
    val pluginFolderFiles = Option(PluginFolder.listFiles())
    val pluginJars = pluginFolderFiles.getOrElse(Array[File]()) filter (_.getName.endsWith(".jar"))
    val classpath = ClassFinder.classpath ++ pluginJars
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses()
    val classMap = ClassFinder.classInfoMap(classes.iterator)
    val pluginsToLoad = ClassFinder.concreteSubclasses(PluginClassName, classMap)

    val loader = new URLClassLoader(pluginJars.map({ f => new URL(s"file:${f.getAbsolutePath}") }), ClassLoader.getSystemClassLoader)

    pluginsToLoad.map(_.name).foreach { pluginClassName =>
      val plugin = loader.loadClass(pluginClassName).newInstance().asInstanceOf[Plugin]

      // Hack to get the plugin name
      // This is ok only because all plugin types must declare a self-type annotation on Plugin
      // I.e. all plugins must also implement the Plugin trait
      Plugins += plugin.name -> plugin
    }
  }

  init()
}
