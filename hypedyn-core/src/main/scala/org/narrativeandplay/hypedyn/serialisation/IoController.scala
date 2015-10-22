package org.narrativeandplay.hypedyn.serialisation

import java.io.{PrintWriter, File}

import scala.io.Source

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
}
