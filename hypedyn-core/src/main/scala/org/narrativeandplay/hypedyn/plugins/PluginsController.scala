package org.narrativeandplay.hypedyn.plugins

import java.io.File
import java.net.{URL, URLClassLoader}

import org.clapper.classutil.ClassFinder

import scala.collection.mutable
import scala.reflect.runtime.universe._

trait PluginsController {
  /**
   * The type of the plugin to be loaded, i.e. the class/trait of the plugin to be loaded
   */
  type T

  /**
   * The fully qualified name of the the class/trait of the plugin to be loaded
   */
  val PluginClassName: String

  private final val PluginFolder = new File("plugins")

  val Plugins = mutable.HashMap.empty[String, T]

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
      val plugin = loader.loadClass(pluginClassName).newInstance().asInstanceOf[T]

      // Hack to get the plugin name
      // This is ok only because all plugin types must declare a self-type annotation on Plugin
      // I.e. all plugins must also implement the Plugin trait
      Plugins += plugin.asInstanceOf[Plugin].name -> plugin
    }
  }

  init()
}

object PluginsController extends PluginsController {
  type T = Plugin
  override val PluginClassName = classOf[Plugin].getCanonicalName
}
