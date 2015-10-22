package org.narrativeandplay.hypedyn

import javafx.beans.value.ObservableValue

import scalafx.Includes._
import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.scene.layout.{VBox, BorderPane}

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.dialogs._
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal}
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinition, ConditionDefinition, Fact}
import org.narrativeandplay.hypedyn.uicomponents._
import org.narrativeandplay.hypedyn.undo.UndoController
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._

/**
 * Entry point for the application
 */
object Main extends JFXApp {
  EventBus
  UndoController
  PluginsController
  CoreEventDispatcher
  UiEventDispatcher
  ClipboardEventDispatcher
  UndoEventDispatcher

  private val icon = new Image(getClass.getResourceAsStream("hypedyn-icon.jpg"))

  private val loadedFilename = new StringProperty("Untitled")
  private val editedMarker = EasyBind map (UiEventDispatcher.isStoryEdited.delegate.asInstanceOf[ObservableValue[Boolean]], if (_: Boolean) "*" else "")

  /**
   * Returns a new file dialog
   */
  def fileDialog = new FileDialog(stage)

  /**
   * Returns a new directory selection dialog
   */
  def directoryDialog = new DirectoryDialog(stage)

  /**
   * Creates a new node editor for editing a node
   *
   * @param dialogTitle The title of the node editor dialog
   * @param conditionDefinitions The list of condition definitions
   * @param actionDefinitions The list of action definitions
   * @param story The story the node belongs to
   * @param nodeToEdit The node to edit
   * @return A new node editor for editing the given node
   */
  def nodeEditor(dialogTitle: String,
                 conditionDefinitions: List[ConditionDefinition],
                 actionDefinitions: List[ActionDefinition],
                 story: Narrative,
                 nodeToEdit: Nodal) = new NodeEditor(dialogTitle, nodeToEdit, conditionDefinitions, actionDefinitions, story, stage)

  /**
   * Creates a new node editor for creating a new node
   *
   * @param dialogTitle The title of the node editor dialog
   * @param conditionDefinitions The list of condition definitions
   * @param actionDefinitions The list of action definitions
   * @param story The story the new node will belong to
   * @return A new node editor for creating a new node
   */
  def nodeEditor(dialogTitle: String,
                 conditionDefinitions: List[ConditionDefinition],
                 actionDefinitions: List[ActionDefinition],
                 story: Narrative) = new NodeEditor(dialogTitle, conditionDefinitions, actionDefinitions, story, stage)

  /**
   * Creates a new fact editor for editing a fact
   *
   * @param dialogTitle The title of the fact editor dialog
   * @param availableFactTypes The fact types available for creation
   * @param factToEdit The fact to edit
   * @return A new fact editor for editing the given fact
   */
  def factEditor(dialogTitle: String, availableFactTypes: List[String], factToEdit: Fact) =
    new FactEditor(dialogTitle, availableFactTypes, factToEdit, stage)

  /**
   * Creates a new fact editor for creating a new fact
   *
   * @param dialogTitle The title of the fact editor dialog
   * @param availableFactTypes The fact types available for creation
   * @return A new fact editor for creating a new fact
   */
  def factEditor(dialogTitle: String, availableFactTypes: List[String]) =
    new FactEditor(dialogTitle, availableFactTypes, stage)

  /**
   * Creates a new story properties editor
   *
   * @param story The story whose properties are to be edited
   * @return A new dialog for editing the given story's properties
   */
  def storyPropertiesEditor(story: Narrative) = new StoryPropertiesDialog(story, stage)

  def editFilename(newFilename: String): Unit = loadedFilename() = newFilename

  stage = new PrimaryStage {
    title <== loadedFilename + editedMarker + " - HypeDyn"
    icons.add(icon)

    // CLose all windows when closing the main application window, i.e. make closing the main window equivalent to
    // an exiting of the program
    onCloseRequest = { evt =>
      UiEventDispatcher.requestExit() foreach { exit =>
        if (exit) Platform.exit() else evt.consume()
      }
    }

    scene = new Scene {
      root = new BorderPane() {
        top = new VBox() {
          children.addAll(Menubar, Toolbar)
        }

        left = Sidebar

        center = CentrePane
      }
    }
  }
}
