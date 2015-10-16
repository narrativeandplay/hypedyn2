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
   * @param exportParentDir The directory to export into
   * @param exportDirName   The name of the export directory to create
   */
  def export(exportParentDir: File, exportDirName: String): Unit = {
    // create the export directory
    val libPath=java.lang.System.getProperty("user.dir")
    val sourcePath=Paths.get(getClass().getResource("export/reader").getPath())
    val destPath=Paths.get(exportParentDir.getAbsolutePath()+"/"+exportDirName)

    // copy the reader into the export directory
    try {
      val opts: EnumSet[FileVisitOption] = EnumSet.of(FileVisitOption.FOLLOW_LINKS)
      val tc: Copy.TreeCopier = new Copy.TreeCopier(sourcePath, destPath, true)
      Files.walkFileTree(sourcePath, opts, Integer.MAX_VALUE, tc)
    } catch {
      case e: Exception => println("exception caught: " + e);
    }
  }

}
