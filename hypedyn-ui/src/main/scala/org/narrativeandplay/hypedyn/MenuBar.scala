package org.narrativeandplay.hypedyn

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
object MenuBar {
  val menuBar = new MenuBar() {
    useSystemMenuBar = true

    menus.addAll(fileMenu, helpMenu)
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
    onAction = (ae: ActionEvent) => {
      Platform.exit()
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
