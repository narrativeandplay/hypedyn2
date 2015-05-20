package org.narrativeandplay.hypedyn

import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, BorderPane}

import org.narrativeandplay.hypedyn.dialogs.{NodeEditor, FileDialog}
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.plugins.storyviewer.NarrativeViewersController
import org.narrativeandplay.hypedyn.story.Nodal

object Main extends JFXApp {
  EventBus
  PluginsController
  CoreEventDispatcher
  UiEventDispatcher
  ClipboardEventDispatcher
  UndoEventDispatcher

  def fileDialog = new FileDialog(stage)
  def nodeEditor(dialogTitle: String, nodeToEdit: Nodal) = new NodeEditor(dialogTitle, nodeToEdit, stage)
  def nodeEditor(dialogTitle: String) = new NodeEditor(dialogTitle, stage)

  stage = new PrimaryStage {
    title = "HypeDyn"

    // CLose all windows when closing the main application window, i.e. make closing the main window equivalent to
    // an exiting of the program
    onCloseRequest = { _ =>
      Platform.exit()
    }

    scene = new Scene {
      root = new BorderPane() {
        top = new VBox() {
          children.addAll(Menubar, Toolbar)
        }

        center = NarrativeViewersController.DefaultViewer
      }
    }
  }
}
