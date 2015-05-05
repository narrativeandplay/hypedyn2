package org.narrativeandplay.hypedyn.serialisation

import java.io.{PrintWriter, File}

import scala.io.Source

object IoController {
  def save(file: File): Unit = {
    val dataToSave = SaveController.getSaveData
    val serialisedData = Serialiser serialise dataToSave
    val fileWriter = new PrintWriter(file)

    fileWriter.write(serialisedData)
    fileWriter.close()
  }

  def load(file: File): Unit = {
    val fileSource = Source.fromFile(file)
    val fileData = fileSource.mkString

    fileSource.close()

    val saveData = Serialiser deserialise fileData
    SaveController.loadSaveData(saveData)
  }
}
