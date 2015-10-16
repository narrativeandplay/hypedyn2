package org.narrativeandplay.hypedyn.serialisation

import java.io.{PrintWriter, File}
import java.nio.file.{FileVisitOption, Files, Paths, StandardCopyOption}
import java.util.EnumSet

/**
 * Created by alex on 16/10/15.
 */
object ExportController {
  /**
   * Export the story
   *
   * @param exportDir The directory to export to
   */
  def export(exportDir: File): Unit = {
    // create the export directory
    val libPath=java.lang.System.getProperty("user.dir") //("java.library.path");
    //val sourcePath=Paths.get(libPath+"/"+"hypedyn-core/org/narrativeandplay/hypedyn/serialisation/export/reader")
    val sourcePath=Paths.get(libPath+"/"+"hypedyn-core/src/main/resources/org/narrativeandplay/hypedyn/serialisation/export/reader")
    val destPath=Paths.get(exportDir.getAbsolutePath()+"/export")

    try {
      val opts: EnumSet[FileVisitOption] = EnumSet.of(FileVisitOption.FOLLOW_LINKS)
      val tc: Copy.TreeCopier = new Copy.TreeCopier(sourcePath, destPath, true)
      Files.walkFileTree(sourcePath, opts, Integer.MAX_VALUE, tc)
    } catch {
      case e: Exception => println("exception caught: " + e);
    }

    // copy the reader into the directory

    //val fileWriter = new PrintWriter(file)

    //fileWriter.close()
  }

}
