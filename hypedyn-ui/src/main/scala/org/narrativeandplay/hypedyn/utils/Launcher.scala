package org.narrativeandplay.hypedyn.utils

import java.io.File

import com.apple.eawt.AppEvent.OpenFilesEvent
import com.apple.eawt.{Application => AppleApplication, OpenFilesHandler}

import org.narrativeandplay.hypedyn.Main
import org.narrativeandplay.hypedyn.events.UiEventDispatcher

object Launcher {
  def main (args: Array[String]): Unit = {
    if (System.isMac) {
      AppleApplication.getApplication.setOpenFileHandler(new OpenFilesHandler {
        override def openFiles(e: OpenFilesEvent): Unit = {
          import scala.collection.JavaConversions._
          e.getFiles foreach { f =>
            if (f.isInstanceOf[File] && f.asInstanceOf[File].exists()) {
              UiEventDispatcher.loadStory(f.asInstanceOf[File])
            }
          }
        }
      })
    }

    Main.main(args)
  }
}
