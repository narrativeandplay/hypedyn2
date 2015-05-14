package org.narrativeandplay.hypedyn.serialisation

import java.io.{PrintWriter, File}

import scala.io.Source

object IoController {
  def save(data: String, file: File): Unit = {
    val fileWriter = new PrintWriter(file)

    fileWriter.write(data)
    fileWriter.close()
  }

  def load(file: File): String = {
    val fileSource = Source.fromFile(file)
    val data = fileSource.mkString

    fileSource.close()

    data
  }
}
