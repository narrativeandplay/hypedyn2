package org.narrativeandplay.hypedyn

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.event.ActionEvent
import scalafx.scene.control.{Menu, MenuBar, MenuItem, SeparatorMenuItem}

import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.keycombinations.KeyCombinations
import org.narrativeandplay.hypedyn.undo.UndoController

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

  private lazy val newStory = new MenuItem("New") {
    accelerator = KeyCombinations.New
  }

  private lazy val openStory = new MenuItem("Open"){
    accelerator = KeyCombinations.Open

    onAction = { ae: ActionEvent =>
      UiEventDispatcher.load()
    }
  }

  private lazy val saveStory = new MenuItem("Save") {
    accelerator = KeyCombinations.Save

    onAction = { ae: ActionEvent =>
      UiEventDispatcher.save()
    }
  }

  private lazy val exit = new MenuItem("Exit") {
    onAction = { actionEvent: ActionEvent =>
      Platform.exit()
    }
  }

  /**
   * Edit Menu
   */
  private lazy val editMenu = new Menu("Edit") {
    items.addAll(undo, redo, new SeparatorMenuItem(), cut, copy, paste)
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

  private lazy val cut = new MenuItem("Cut") {
    accelerator = KeyCombinations.Cut

    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.selectedNodeId foreach (EventBus send CutNodeRequest(_))
    }
  }

  private lazy val copy = new MenuItem("Copy") {
    accelerator = KeyCombinations.Copy

    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.selectedNodeId foreach (EventBus send CopyNodeRequest(_))
    }
  }

  private lazy val paste = new MenuItem("Paste") {
    accelerator = KeyCombinations.Paste

    onAction = { actionEvent: ActionEvent =>
      EventBus send PasteNodeRequest
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
