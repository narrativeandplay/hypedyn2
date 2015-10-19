package org.narrativeandplay.hypedyn.serialisation

import java.io.{File}
import java.nio.file.{Paths}

/**
 * Created by alex on 16/10/15.
 */
object ExportController {
  /**
   * Export the story
   *
   * @param exportParentDir The directory to export into
   * @param exportDirName   The name of the export directory to create
   */
  def export(exportParentDir: File, exportDirName: String): Unit = {
    val sourcePath=getClass().getResource("export/reader").toURI
    val destPath=Paths.get(exportParentDir.getAbsolutePath()+"/"+exportDirName)
    Copy.copyFromJar(sourcePath, destPath)
  }
}
