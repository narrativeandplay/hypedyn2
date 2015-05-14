package org.narrativeandplay.hypedyn

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, BorderPane}

import org.narrativeandplay.hypedyn.dialogs.FileDialog
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.plugins.storyviewer.NarrativeViewersController

object Main extends JFXApp {
  EventBus
  PluginsController
  CoreEventDispatcher
  UiEventDispatcher
  ClipboardEventDispatcher
  UndoEventDispatcher

  def fileDialog = new FileDialog(stage)

  stage = new PrimaryStage {
    title = "HypeDyn"

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
