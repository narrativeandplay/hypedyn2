package org.narrativeandplay.hypedyn.core.serialisation

import java.net.JarURLConnection

import scala.io.Source

import better.files._

import org.apache.commons.io.FileUtils

/**
 * Controller handling file read/write
 */
object IoController {
  /**
   * Write data to file
   *
   * @param data The data to write to file
   * @param file The file to write to
   */
  def write(data: String, file: File): Unit = {
    file.overwrite(data)
  }

  /**
   * Read data from file
   *
   * @param file The file to read
   * @return The data in the file as a string
   */
  def read(file: File): String = {
    file.contentAsString
  }

  /**
   * Copies the contents of a resource folder from inside the classpath to the filesystem. The name of the folder must
   * end in a '/'
   *
   * Adapted from http://stackoverflow.com/a/2993908
   *
   * @param resourceName The resource folder whose contents are to be copied; if a relative resource path is given,
   *                     it will be relative to org.narrativeandplay.hypedyn.core.serialisation
   * @param directory The directory where the files are to be copied to, if the folder does not exist, it will be
   *                  created
   */
  def copyResourceToFilesystem(resourceName: String, directory: File): Unit = {
    directory.createIfNotExists(asDirectory = true, createParents = true)

    val jarConnection = getClass.getResource(resourceName).openConnection().asInstanceOf[JarURLConnection]
    val jarFile = jarConnection.getJarFile

    if (!jarConnection.getJarEntry.isDirectory)
      throw new IllegalArgumentException("Resource to be copied must be a directory")

    import scala.collection.JavaConverters._
    jarFile.entries().asScala foreach { entry =>
      if (entry.getName startsWith jarConnection.getEntryName) {
        val filename = entry.getName replace (jarConnection.getEntryName, "")

        entry match {
          case file if !entry.isDirectory =>
            val in = jarFile.getInputStream(entry)
            directory.createChild(filename, createParents = true).outputStream foreach { output =>
              in.pipeTo(output)
            }

            in.close()

          case dir =>
            directory.createChild(filename, asDirectory = true, createParents = true)
        }
      }
    }
  }
}
