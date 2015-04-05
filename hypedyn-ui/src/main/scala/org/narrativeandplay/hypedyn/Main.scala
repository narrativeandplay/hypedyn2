package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.events.{UIEventDispatcher, EventBus}
import org.narrativeandplay.hypedyn.plugins.storyviewer.StoryViewersController
import org.narrativeandplay.hypedyn.story.StoryController

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, BorderPane}

object Main extends JFXApp {
  EventBus
  StoryController
  UIEventDispatcher

  stage = new PrimaryStage {
    title = "HypeDyn"

    scene = new Scene {
      root = new BorderPane() {
        top = new VBox() {
          children.addAll(MenuBar.menuBar, Toolbar.toolbar)
        }

        center = StoryViewersController.defaultViewer
      }
    }
  }
}
