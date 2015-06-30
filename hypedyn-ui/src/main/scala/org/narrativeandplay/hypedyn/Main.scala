package org.narrativeandplay.hypedyn

import scalafx.Includes._
import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.scene.layout.{VBox, BorderPane}

import org.narrativeandplay.hypedyn.dialogs.{FactEditor, NodeEditor, FileDialog}
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.plugins.storyviewer.NarrativeViewersController
import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.uicomponents.FactViewer
import org.narrativeandplay.hypedyn.undo.UndoController

object Main extends JFXApp {
  EventBus
  UndoController
  PluginsController
  CoreEventDispatcher
  UiEventDispatcher
  ClipboardEventDispatcher
  UndoEventDispatcher

  private val icon = new Image(getClass.getResourceAsStream("hypedyn-icon.jpg"))

  def fileDialog = new FileDialog(stage)
  def nodeEditor(dialogTitle: String, nodeToEdit: Nodal) = new NodeEditor(dialogTitle, nodeToEdit, Nil, Nil, stage)
  def nodeEditor(dialogTitle: String) = new NodeEditor(dialogTitle, Nil, Nil, stage)
  def factEditor(dialogTitle: String, availableFactTypes: List[String], factToEdit: Fact) =
    new FactEditor(dialogTitle, availableFactTypes, factToEdit, stage)
  def factEditor(dialogTitle: String, availableFactTypes: List[String]) =
    new FactEditor(dialogTitle, availableFactTypes, stage)

  stage = new PrimaryStage {
    title = "HypeDyn"
    icons.add(icon)

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

        left = FactViewer

        center = NarrativeViewersController.DefaultViewer
      }
    }
  }
}
