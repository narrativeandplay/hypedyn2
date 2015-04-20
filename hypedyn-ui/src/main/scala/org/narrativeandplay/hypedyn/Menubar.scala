package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.keycombinations.KeyCombinations
import org.narrativeandplay.hypedyn.undo.UndoController

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.{SeparatorMenuItem, MenuBar, MenuItem, Menu}

/**
 * Object containing the menu bar
 *
 * All menu items are declared after the menu itself, for organisation purposes. As such all menu items must be lazily
 * evaluated, to guarantee correct execution
 */
object Menubar {
  val menuBar = new MenuBar() {
    useSystemMenuBar = true

    menus.addAll(fileMenu, editMenu, helpMenu)
  }

  /**
   * File Menu
   */
  private lazy val fileMenu = new Menu("File") {
    items.addAll(newStory, openStory, saveStory, new SeparatorMenuItem(), exit)
  }

  private lazy val newStory = new MenuItem("New")
  private lazy val openStory = new MenuItem("Open")
  private lazy val saveStory = new MenuItem("Save")
  private lazy val exit = new MenuItem("Exit") {
    onAction = { actionEvent: ActionEvent =>
      Platform.exit()
    }
  }

  /**
   * Edit Menu
   */
  private lazy val editMenu = new Menu("Edit") {
    items.addAll(undo, redo)
  }
  private lazy val undo = new MenuItem("Undo") {
    accelerator = KeyCombinations.Undo

    onAction = { actionEvent: ActionEvent =>
      UndoController.undo()
    }
  }

  private lazy val redo = new MenuItem("Redo") {
    accelerator = if (System.getProperty("os.name") == "Windows") KeyCombinations.RedoWin else KeyCombinations.RedoUnix

    onAction = { actionEvent: ActionEvent =>
      UndoController.redo()
    }
  }

  /**
   * Help Menu
   */
  private lazy val helpMenu = new Menu("Help") {
    items.addAll(about)
  }

  private lazy val about = new MenuItem("About")
}
