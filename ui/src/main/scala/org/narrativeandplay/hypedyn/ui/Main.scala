package org.narrativeandplay.hypedyn.ui

import java.lang.{System => Sys}
import javafx.beans.value.ObservableValue
import javafx.scene.{input => jfxsi}

import scala.util.Properties

import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.{BorderPane, VBox}

import better.files._
import org.gerweck.scalafx.util._
import com.sun.glass.ui
import com.sun.glass.ui.Application.EventHandler
import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

import org.narrativeandplay.hypedyn.api.events.EventBus
import org.narrativeandplay.hypedyn.api.logging.Logger
import org.narrativeandplay.hypedyn.api.story.{Narrative, Nodal}
import org.narrativeandplay.hypedyn.api.story.rules.{ActionDefinition, ConditionDefinition, Fact}
import org.narrativeandplay.hypedyn.api.utils.System
import org.narrativeandplay.hypedyn.ui.dialogs._
import org.narrativeandplay.hypedyn.core.events._
import org.narrativeandplay.hypedyn.core.plugins.PluginsController
import org.narrativeandplay.hypedyn.ui.server.Server
import org.narrativeandplay.hypedyn.ui.components._
import org.narrativeandplay.hypedyn.core.undo.UndoController
import org.narrativeandplay.hypedyn.ui.events.UiEventDispatcher

/**
 * Entry point for the application
 */
object Main extends JFXApp {
  Properties.setProp("hypedyn.log.location", System.LogLocation.toString())

  EventBus
  UndoController
  PluginsController
  CoreEventDispatcher
  UiEventDispatcher
  ClipboardEventDispatcher
  UndoEventDispatcher
  Logger
  Server

  Logger.info("Java version: " + Properties.javaVersion)

  Thread.currentThread().setUncaughtExceptionHandler({ (_, throwable) =>
    // Most exceptions will show up as a `OnErrorNotImplementedException` because
    // of RxScala, so we grab the actual exception instead of allowing it to
    // pollute the logs
    val actualThrowable = throwable match {
      case e: rx.exceptions.OnErrorNotImplementedException => e.getCause
      case e => e
    }

    Logger.error("Unhandled Exception:", actualThrowable)
  })

  private val icon = new Image(getClass.getResourceAsStream("hypedyn-icon.jpg"))

  private val loadedFilename = new StringProperty("Untitled")
  // Because scalafx-utils returns ReadOnlyObjectProperties, their usage in a string returns a bad toString form
  // So we cast to an ObservableValue[String] to make the ReadOnlyObjectProperty[String] be useful in a binding
  private val editedMarker: ObservableValue[String] = UiEventDispatcher.isStoryEdited map (if (_) "*" else "")


  private val refreshStream = SerializedSubject(PublishSubject[Unit]())
  def refreshRecent = refreshStream

  private var lastKeypressTime = Sys.currentTimeMillis()

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

  def aboutDialog = new Alert(Alert.AlertType.Information) {
    initOwner(stage)
    title = "About HypeDyn 2"
    headerText = "HypeDyn 2"
    graphic = new ImageView(icon)
    contentText =
      """Hypertext Fiction Editor
        |Version 1.0
      """.stripMargin
  }

  def loadedFileName_=(newFilename: String): Unit = loadedFilename() = newFilename
  def loadedFileName = loadedFilename()

  def runInBrowser(filePath: File, fileToRun: String): Unit = {
    val fileToLoad = s"${Server.address}/$fileToRun"
    Server.storyPath = filePath.toString()

    hostServices.showDocument(fileToLoad)
  }

  stage = new PrimaryStage {
    title <== loadedFilename + editedMarker + " - HypeDyn 2"
    icons.add(icon)

    val mainStageFocused = focused

    // CLose all windows when closing the main application window, i.e. make closing the main window equivalent to
    // an exiting of the program
    onCloseRequest = { evt =>
      UiEventDispatcher requestExit { exit =>
        if (exit) {
          Logger.info("Exiting HypeDyn 2 via main window close")
          Platform.runLater(Server.shutdown())
          Platform.exit()
        }
        else {
          Logger.info("Window close exit request cancelled/failed.")
          evt.consume()
        }
      }
    }

    addEventFilter(KeyEvent.KeyPressed, { event: jfxsi.KeyEvent =>
      val timeDiff = Sys.currentTimeMillis() - lastKeypressTime
      // Because OS X does something stupid by firing multiple events for a single Equals key press, we add a timestamp
      // to track when the last time the zoom was triggered, and allow it to zoom only if it was at least 2 ms after
      // the last zoom time.
      if (event.shortcutDown && timeDiff > 1) {
        event.code match {
          case KeyCode.Add | KeyCode.Equals => UiEventDispatcher.requestZoom(0.1)
          case KeyCode.Minus | KeyCode.Subtract => UiEventDispatcher.requestZoom(-0.1)
          case KeyCode.Numpad0 | KeyCode.Digit0 => UiEventDispatcher.requestZoomReset()
          case _ =>
        }
      }
      event.code match {
        case KeyCode.Delete | KeyCode.BackSpace =>
          UiEventDispatcher.requestDeleteNode()
        case _ =>
      }
      lastKeypressTime = Sys.currentTimeMillis()
    })

    scene = new Scene {
      root = new BorderPane() {
        top = new VBox() {
          children.addAll(new Menubar(mainStageFocused), Toolbar)
        }

        left = Sidebar

        center = CentrePane
      }
    }
  }

  if (parameters.raw.length == 1) {
    val fileToOpen = File(parameters.raw.head)

    if (fileToOpen.exists()) UiEventDispatcher.loadStory(fileToOpen)
  }

  private val app = ui.Application.GetApplication()
  app.setEventHandler(new EventHandler {
    override def handleOpenFilesAction(app: ui.Application, time: Long, filepaths: Array[String]): Unit = {
      super.handleOpenFilesAction(app, time, filepaths)
      // The filtering is for when HypeDyn is run directly from the IDE (and possibly via the
      // command line using Gradle) because it's passed the classname to run, and without removing
      //  that the application would crash
      val files = filepaths filterNot (_ == "org.narrativeandplay.hypedyn.Main") map (File(_))
      if (files.length > 0) {
        UiEventDispatcher.loadStory(files.head)
      }
    }

    override def handleQuitAction(app: ui.Application, time: Long): Unit = {
      super.handleQuitAction(app, time)

      UiEventDispatcher requestExit { exit =>
        if (exit) {
          Logger.info("Exiting HypeDyn 2 via Cmd-Q")
          Platform.runLater(Server.shutdown())
          Platform.exit()
        }
        else {
          Logger.info("Cmd-Q exit request cancelled/failed.")
        }
      }
    }
  })
}
