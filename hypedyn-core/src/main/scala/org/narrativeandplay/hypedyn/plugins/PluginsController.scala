package org.narrativeandplay.hypedyn.plugins

import java.io.File
import java.net.{URL, URLClassLoader}

import scala.collection.mutable

import org.clapper.classutil.ClassFinder

object PluginsController {
  val PluginClassName = classOf[Plugin].getCanonicalName

  private final val PluginFolder = new File("plugins")

  private val _plugins = mutable.HashMap.empty[String, Plugin]

  def plugins = _plugins.toMap

  private def init(): Unit = {
    val pluginFolderFiles = Option(PluginFolder.listFiles())
    val pluginJars = pluginFolderFiles getOrElse Array.empty[File] filter (_.getName.endsWith(".jar"))
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
