package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.events.{EventDispatcher, UiEventDispatcher, EventBus}
import org.narrativeandplay.hypedyn.plugins.storyviewer.StoryViewersController

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, BorderPane}

object Main extends JFXApp {
  EventBus
  EventDispatcher
  UiEventDispatcher

  UiEventDispatcher.mainStage = stage

  stage = new PrimaryStage {
    title = "HypeDyn"

    scene = new Scene {
      root = new BorderPane() {
        top = new VBox() {
          children.addAll(Menubar.menuBar, Toolbar.toolbar)
        }

        center = StoryViewersController.defaultViewer
      }
    }
  }
}
