package org.narrativeandplay.hypedyn.events

import java.io.File

import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, BooleanProperty}
import scalafx.scene.control.{ButtonType, Alert}

import rx.lang.scala.Observable

import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.Main
import org.narrativeandplay.hypedyn.dialogs.NodeEditor
import org.narrativeandplay.hypedyn.story.{Nodal, NodeId}
import org.narrativeandplay.hypedyn.uicomponents.FactViewer
import org.narrativeandplay.hypedyn.story.InterfaceToUiImplementation._
import org.narrativeandplay.hypedyn.utils.HypedynPreferences

/**
 * Dispatcher for UI events
 *
 * Any component that needs to send events should go through this dispatcher
 */
object UiEventDispatcher {
  val UiEventSourceIdentity = "UI"
  val selectedNode = ObjectProperty(Option.empty[NodeId])
  private val openedNodeEditors = ArrayBuffer.empty[NodeEditor]
  val isStoryEdited = BooleanProperty(false)
  val undoAvailable = BooleanProperty(false)
  val redoAvailable = BooleanProperty(false)

  EventBus.NewNodeResponses foreach { response =>
    val editor = Main.nodeEditor("New Node", response.conditionDefinitions, response.actionDefinitions, response.story)

    editor.onCloseRequest = { _ =>
      openedNodeEditors -= editor
    }

    openedNodeEditors += editor
    editor.show()
  }
  EventBus.EditNodeResponses foreach { response =>
    openedNodeEditors find (_.id == response.node.id) match {
      case Some(editor) => editor.dialogPane().scene().window().requestFocus()
      case None =>
        val editor = Main.nodeEditor("Edit Node", response.conditionDefinitions, response.actionDefinitions, response.story, response.node)

        editor.onCloseRequest = { _ =>
          openedNodeEditors -= editor
        }

        openedNodeEditors += editor
        editor.show()
    }
  }
  EventBus.DeleteNodeResponses foreach { evt => EventBus.send(DestroyNode(evt.node, UiEventSourceIdentity)) }

  EventBus.NewFactResponses foreach { res =>
    val newFact = Main.factEditor("New Fact", res.factTypes).showAndWait()

    newFact foreach { f => EventBus.send(CreateFact(f, UiEventSourceIdentity)) }
  }
  EventBus.EditFactResponses foreach { res =>
    val editedFact = Main.factEditor("Edit Fact", res.factTypes, res.fact).showAndWait()

    editedFact foreach { f => EventBus.send(UpdateFact(res.fact, f, UiEventSourceIdentity)) }
  }
  EventBus.DeleteFactResponses foreach { res => EventBus.send(DestroyFact(res.fact, UiEventSourceIdentity)) }

  EventBus.SaveResponses foreach { evt =>
    evt.loadedFile match {
      case Some(file) => EventBus.send(SaveToFile(file, UiEventSourceIdentity))
      case None =>
        val fileToSaveTo = Main.fileDialog.showSaveFileDialog()

        fileToSaveTo match {
          case Some(f) => EventBus.send(SaveToFile(f, UiEventSourceIdentity))
          case None => EventBus.send(SaveCancelled(UiEventSourceIdentity))
        }
    }
  }
  EventBus.SaveAsResponses foreach { _ =>
    val fileToSaveTo = Main.fileDialog.showSaveFileDialog()

    fileToSaveTo match {
      case Some(f) => EventBus.send(SaveToFile(f, UiEventSourceIdentity))
      case None => EventBus.send(SaveCancelled(UiEventSourceIdentity))
    }
  }
  EventBus.LoadResponses foreach { _ =>
    val fileToLoad = Main.fileDialog.showOpenFileDialog()

    fileToLoad foreach { f =>
      EventBus.send(LoadFromFile(f, UiEventSourceIdentity))
    }
  }

  EventBus.ExportResponses foreach { evt =>
    // get location to export to (a directory)
    val dirToSaveTo = Main.directoryDialog.showDialog()

    dirToSaveTo foreach { d => EventBus.send(ExportToFile(d, Main.loadedFileName, UiEventSourceIdentity)) }
  }
  // actually run the story
  EventBus.RunResponses foreach { evt => EventBus.send(RunStory(evt.filePath, evt.fileToRun, UiEventSourceIdentity)) }
  EventBus.RunStoryEvents foreach { evt =>
    Main.runInBrowser(evt.filePath, evt.fileToRun)

    EventBus.send(StoryRan(UiEventSourceIdentity))
  }

  EventBus.StoryLoadedEvents foreach { evt =>
    FactViewer.facts.clear()
    evt.story.facts foreach { f => FactViewer.facts += f }
  }
  EventBus.StoryUpdatedEvents foreach { evt =>
    openedNodeEditors foreach (_.story() = evt.story)
  }
  EventBus.FileSavedEvents foreach { evt =>
    HypedynPreferences.recentFiles +:= evt.file
    Main.refreshRecent.onNext(())

    Main.loadedFileName = evt.file.getName
  }
  EventBus.FileLoadedEvents foreach { evt =>
    evt.fileOption foreach { f =>
      HypedynPreferences.recentFiles +:= f
      Main.refreshRecent.onNext(())
    }

    Main.loadedFileName = evt.fileOption map (_.getName) getOrElse "Untitled"
  }

  EventBus.NewStoryResponses foreach { _ => EventBus.send(CreateStory(src = UiEventSourceIdentity)) }
  EventBus.EditStoryPropertiesResponses foreach { evt =>
    val editedProperties = Main.storyPropertiesEditor(evt.story).showAndWait()

    editedProperties foreach { case (title, author, desc, metadata) =>
      EventBus.send(UpdateStoryProperties(title, author, desc, metadata, UiEventSourceIdentity))
    }
  }

  EventBus.NodeCreatedEvents foreach { evt =>
    openedNodeEditors find (_.id == evt.originalNodeData.id) foreach (_.updateNodeData(evt.node))
  }
  EventBus.NodeUpdatedEvents foreach { evt =>
    openedNodeEditors find (_.id == evt.node.id) foreach (_.updateNodeData(evt.updatedNode))
  }
  EventBus.NodeDestroyedEvents foreach { evt =>
    openedNodeEditors find (_.id == evt.node.id) foreach (_.close())
  }

  EventBus.UiNodeSelectedEvents foreach { evt => selectedNode() = Some(evt.id) }
  EventBus.UiNodeDeselectedEvents foreach { _ => selectedNode() = None }

  EventBus.FactCreatedEvents foreach { evt => FactViewer.add(evt.fact) }
  EventBus.FactUpdatedEvents foreach { evt => FactViewer.update(evt.fact, evt.updatedFact) }
  EventBus.FactDestroyedEvents foreach { evt => FactViewer.remove(evt.fact) }

  EventBus.FileStatusEvents foreach { evt =>
    isStoryEdited() = evt.isChanged
  }

  EventBus.UndoStatusEvents foreach { evt =>
    undoAvailable() = evt.isAvailable
  }
  EventBus.RedoStatusEvents foreach { evt =>
    redoAvailable() = evt.isAvailable
  }

  EventBus.ErrorEvents foreach { err =>
    new Alert(Alert.AlertType.Error, err.msg, ButtonType.OK).showAndWait()
  }

  def requestNewNode(): Unit = {
    EventBus.send(NewNodeRequest(UiEventSourceIdentity))
  }
  def requestEditNode(): Unit = {
    selectedNode() foreach { id => EventBus.send(EditNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestDeleteNode(): Unit = {
    selectedNode() foreach { id => EventBus.send(DeleteNodeRequest(id, UiEventSourceIdentity)) }
  }

  def requestNewFact(): Unit = {
    EventBus.send(NewFactRequest(UiEventSourceIdentity))
  }
  def requestEditFact(): Unit = {
    Option(FactViewer.selectionModel().selectedItem()) foreach { f =>
      EventBus.send(EditFactRequest(f.id, UiEventSourceIdentity))
    }
  }
  def requestEditFact(fact: Fact): Unit = {
    EventBus.send(EditFactRequest(fact.id, UiEventSourceIdentity))
  }
  def requestDeleteFact(): Unit = {
    Option(FactViewer.selectionModel().selectedItem()) foreach { f =>
      EventBus.send(DeleteFactRequest(f.id, UiEventSourceIdentity))
    }
  }

  def requestNewStory(): Unit = {
    EventBus.send(NewStoryRequest(UiEventSourceIdentity))
  }
  def requestEditStoryProperties(): Unit = {
    EventBus.send(EditStoryPropertiesRequest(UiEventSourceIdentity))
  }

  def requestSave(): Unit = {
    EventBus.send(SaveRequest(UiEventSourceIdentity))
  }
  def requestSaveAs(): Unit = {
    EventBus.send(SaveAsRequest(UiEventSourceIdentity))
  }
  def requestLoad(): Unit = {
    EventBus.send(LoadRequest(UiEventSourceIdentity))
  }

  def requestExport(): Unit = {
    EventBus.send(ExportRequest(UiEventSourceIdentity))
  }
  def requestRunStory(): Unit = {
    EventBus.send(RunRequest(UiEventSourceIdentity))
  }

  def requestCut(): Unit = {
    selectedNode() foreach { id => EventBus.send(CutNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestCopy(): Unit = {
    selectedNode() foreach { id => EventBus.send(CopyNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestPaste(): Unit = {
    EventBus.send(PasteNodeRequest(UiEventSourceIdentity))
  }

  def requestUndo(): Unit = {
    EventBus.send(UndoRequest(UiEventSourceIdentity))
  }
  def requestRedo(): Unit = {
    EventBus.send(RedoRequest(UiEventSourceIdentity))
  }

  def createNode(newNode: Nodal): Unit = {
    EventBus.send(CreateNode(newNode, UiEventSourceIdentity))
  }
  def updateNode(originalNode: Nodal) = { editedNode: Nodal =>
    EventBus.send(UpdateNode(originalNode, editedNode, UiEventSourceIdentity))
  }

  def loadStory(file: File): Unit = {
    EventBus.send(LoadFromFile(file, UiEventSourceIdentity))
  }

  /**
   * Checks to see if the current story is changed before taking an action that results in changing the currently
   * loaded story
   *
   * @param f The action to perform
   */
  def requestExit(f: Boolean => Unit): Unit = {
    // Bind the action to perform if a save is requested first before checking
    // This ensures that there won't be a race condition with the save request
    val subscription = EventBus.StorySavedEvents merge EventBus.SaveCancelledEvents subscribe { evt: Completion =>
      evt match {
        case e: StorySaved => f(true)
        case e: SaveCancelled => f(false)
        case _ => assert(false, "Impossible match")
      }
    }

    isStoryEdited() match {
      case true =>
        val Yes = new ButtonType("Yes")
        val No = new ButtonType("No")
        val confirmExit = new Alert(Alert.AlertType.Confirmation) {
          initOwner(Main.stage)

          title = "Unsaved Project"
          headerText = None
          contentText = "The current project has not been saved.\nDo you want to save it?"

          buttonTypes = Seq(Yes, No, ButtonType.Cancel)
        }

        confirmExit.showAndWait() match {
          case Some(Yes) => requestSave()
          case Some(No) => f(true)
          case _ => f(false)
        }
      case false => f(true)
    }

    // Clear the earlier binding, to prevent multiple executions of the callback
    subscription.unsubscribe()
  }

}
