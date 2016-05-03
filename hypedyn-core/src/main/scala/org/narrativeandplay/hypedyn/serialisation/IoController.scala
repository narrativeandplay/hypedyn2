package org.narrativeandplay.hypedyn.serialisation

import java.io.{PrintWriter, File}
import java.net.JarURLConnection

import scala.io.Source

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
    val fileWriter = new PrintWriter(file)

    fileWriter.write(data)
    fileWriter.close()
  }

  /**
   * Read data from file
   *
   * @param file The file to read
   * @return The data in the file as a string
   */
  def read(file: File): String = {
    val fileSource = Source.fromFile(file)
    val data = fileSource.mkString

    fileSource.close()

    data
  }

  /**
   * Copies the contents of a resource folder from inside the classpath to the filesystem. The name of the folder must
   * end in a '/'
   *
   * Adapted from http://stackoverflow.com/a/2993908
   *
   * @param resourceName The resource folder whose contents are to be copied; if a relative resource path is given,
   *                     it will be relative to org.narrativeandplay.hypedyn.serialisation
   * @param directory The directory where the files are to be copied to, if the folder does not exist, it will be
   *                  created
   */
  def copyResourceToFilesystem(resourceName: String, directory: File): Unit = {
    if (!directory.exists()) {
      directory.mkdirs()
    }

    val jarConnection = getClass.getResource(resourceName).openConnection().asInstanceOf[JarURLConnection]
    val jarFile = jarConnection.getJarFile

    if (!jarConnection.getJarEntry.isDirectory)
      throw new IllegalArgumentException("Resource to be copied must be a directory")

    import scala.collection.JavaConversions._
    jarFile.entries() foreach { entry =>
      if (entry.getName startsWith jarConnection.getEntryName) {
        val filename = entry.getName replace (jarConnection.getEntryName, "")

        if (!entry.isDirectory) {
          val in = jarFile.getInputStream(entry)
          FileUtils.copyInputStreamToFile(in, new File(directory, filename))
          in.close()
        }
        else {
          if (!directory.exists()) {
            new File(directory, filename).mkdir()
          }
        }
      }
    }
  }
}
