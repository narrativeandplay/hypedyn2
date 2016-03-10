package org.narrativeandplay.hypedyn

import java.io.File

import com.sun.glass.ui
import com.sun.glass.ui.Application.EventHandler
import org.fxmisc.easybind.EasyBind
import org.narrativeandplay.hypedyn.dialogs._
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinition, ConditionDefinition, Fact}
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal}
import org.narrativeandplay.hypedyn.uicomponents._
import org.narrativeandplay.hypedyn.undo.UndoController
import org.narrativeandplay.hypedyn.utils.{System => Sys}
import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.StringProperty
import scalafx.scene.Scene
import scalafx.scene.control.Alert
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, VBox}

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
  Logger

  Thread.currentThread().setUncaughtExceptionHandler({ (_, throwable) =>
    // Most exceptions will show up as a `OnErrorNotImplementedException` because
    // of RxScala, so we grab the actual exception instead of allowing it to
    // pollute the logs
    val actualThrowable = throwable match {
      case e: rx.exceptions.OnErrorNotImplementedException => e.getCause
      case e => e
    }

    Logger.error("", actualThrowable)
  })

  private val icon = new Image(getClass.getResourceAsStream("hypedyn-icon.jpg"))

  private val loadedFilename = new StringProperty("Untitled")
  private val editedMarker = EasyBind monadic UiEventDispatcher.isStoryEdited map[String] (if (_) "*" else "")

  private val refreshStream = SerializedSubject(PublishSubject[Unit]())
  def refreshRecent = refreshStream

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

  def runInBrowser(file: File): Unit = {
    val runtime = Runtime.getRuntime
    val filePath = file.getAbsolutePath

    if (Sys.isWindows) {
      runtime.exec(s"rundll32 url.dll,FileProtocolHandler $filePath")
    }
    else if (Sys.isMac) {
      runtime.exec(s"open $filePath")
    }
    else {
      runtime.exec(s"xdg-open $filePath")
    }
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
          Platform.exit()
        }
        else {
          Logger.info("Window close exit request cancelled/failed.")
          evt.consume()
        }
      }
    }

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
    val fileToOpen = new File(parameters.raw.head)

    if (fileToOpen.exists()) UiEventDispatcher.loadStory(fileToOpen)
  }

  private val app = ui.Application.GetApplication()
  app.setEventHandler(new EventHandler {
    override def handleOpenFilesAction(app: ui.Application, time: Long, filepaths: Array[String]): Unit = {
      super.handleOpenFilesAction(app, time, filepaths)
      // The filtering is for when HypeDyn is run directly from the IDE (and possibly via the
      // command line using Gradle) because it's passed the classname to run, and without removing
      //  that the application would crash
      val files = filepaths filterNot (_ == "org.narrativeandplay.hypedyn.Main") map (new File(_))
      if (files.length > 0) {
        UiEventDispatcher.loadStory(files.head)
      }
    }

    override def handleQuitAction(app: ui.Application, time: Long): Unit = {
      super.handleQuitAction(app, time)

      UiEventDispatcher requestExit { exit =>
        if (exit) {
          Logger.info("Exiting HypeDyn 2 via Cmd-Q")
          Platform.exit()
        }
        else {
          Logger.info("Cmd-Q exit request cancelled/failed.")
        }
      }
    }
  })
}
