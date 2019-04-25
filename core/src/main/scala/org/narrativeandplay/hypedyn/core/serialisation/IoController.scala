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
   * @param resourceFolder The resource folder whose contents are to be copied; if a relative resource path is given,
   *                     it will be relative to org.narrativeandplay.hypedyn.core.serialisation
   * @param destinationDir The directory where the files are to be copied to, if the folder does not exist, it will be
   *                       created
   */
  def copyResourceFolderToFilesystem(resourceFolder: String, destinationDir: File): Unit = {
    destinationDir.createIfNotExists(asDirectory = true, createParents = true)

    val jarConnection = getClass.getResource(resourceFolder).openConnection().asInstanceOf[JarURLConnection]
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
            destinationDir.createChild(filename, createParents = true).outputStream foreach { output =>
              in.pipeTo(output)
            }

            in.close()

          case dir =>
            destinationDir.createChild(filename, asDirectory = true, createParents = true)
        }
      }
    }
  }

  /**
   * Copies a resource from inside the classpath to the filesystem
   *
   * Adapted from http://stackoverflow.com/a/2993908
   *
   * @param resource The resource to be copied; if a relative resource path is given,
   *                 it will be relative to org.narrativeandplay.hypedyn.core.serialisation
   * @param destination The destination where the file is to be copied to; if it ends with a '/' it is treated as a
   *                    folder, the copied file will retain the name of the resource, otherwise, it will be given the
   *                    filename
   */
  def copyResourceToFilesystem(resource: String, destination: File): Unit = {
    val isDir = destination.name.endsWith("/")
    if (isDir) { // The destination is a folder
      destination.createIfNotExists(true, true)
    }
    else {
      destination.createIfNotExists(createParents = true)
    }

    val jarConnection = getClass.getResource(resource).openConnection().asInstanceOf[JarURLConnection]
    val jarFile = jarConnection.getJarFile

    if (jarConnection.getJarEntry.isDirectory)
      throw new IllegalArgumentException("Resource to be copied must not be a directory")

    val file = if (isDir) destination / resource.split("/").last else destination

    val in = getClass.getResourceAsStream(resource)
    file.outputStream foreach { output =>
      in.pipeTo(output)
    }
  }
}
