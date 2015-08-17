package org.narrativeandplay.hypedyn.events

import scalafx.Includes._

import org.narrativeandplay.hypedyn.Main
import org.narrativeandplay.hypedyn.dialogs.NodeEditor
import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.uicomponents.FactViewer

/**
 * Dispatcher for UI events
 *
 * Any component that needs to send events should go through this dispatcher
 */
object UiEventDispatcher {
  val UiEventSourceIdentity = "UI"
  private var selectedNode: Option[NodeId] = None
  private var openedNodeEditors = Map.empty[NodeId, NodeEditor]

  EventBus.NewNodeResponses foreach { response =>
    val editor = Main.nodeEditor("New Node", response.conditionDefinitions, response.actionDefinitions, response.story)

    editor.result onChange { (_, _, newNode) =>
      // The onChange listener takes 3 values: the observable whose value changes, the old value of the observable,
      // and the new value of the observable. Due to ScalaFX not properly wrapping JavaFX, and there being no guarantee
      // from JavaFX that the new value will not be null, the new value is first wrapped into an Option for null-safety,
      // then processed.
      Option(newNode) foreach { n => EventBus.send(CreateNode(n, UiEventSourceIdentity)) }
    }

    editor.show()
  }
  EventBus.EditNodeResponses foreach { response =>
    openedNodeEditors get response.node.id match {
      case Some(editor) => editor.dialogPane.value.getScene.getWindow.requestFocus()
      case None =>
        val editor = Main.nodeEditor("Edit Node", response.conditionDefinitions, response.actionDefinitions, response.story, response.node)

        editor.result onChange { (_, _, editedNode) =>
          // The onChange listener takes 3 values: the observable whose value changes, the old value of the observable,
          // and the new value of the observable. Due to ScalaFX not properly wrapping JavaFX, and there being no guarantee
          // from JavaFX that the new value will not be null, the new value is first wrapped into an Option for null-safety,
          // then processed.
          Option(editedNode) foreach { n =>
            EventBus.send(UpdateNode(response.node, n, UiEventSourceIdentity))
          }
        }

        editor.onCloseRequest = { _ =>
          openedNodeEditors -= response.node.id
        }

        openedNodeEditors += response.node.id -> editor
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

        fileToSaveTo foreach { f => EventBus.send(SaveToFile(f, UiEventSourceIdentity)) }
    }
  }
  EventBus.SaveAsResponses foreach { _ =>
    val fileToSaveTo = Main.fileDialog.showSaveFileDialog()

    fileToSaveTo foreach { f => EventBus.send(SaveToFile(f, UiEventSourceIdentity)) }
  }
  EventBus.LoadResponses foreach { _ =>
    val fileToLoad = Main.fileDialog.showOpenFileDialog()

    fileToLoad foreach { f => EventBus.send(LoadFromFile(f, UiEventSourceIdentity)) }
  }

  EventBus.StoryLoadedEvents foreach { evt =>
    FactViewer.facts.clear()
    evt.story.facts foreach { f => FactViewer.facts += f }
  }

  EventBus.NewStoryResponses foreach { _ => EventBus.send(CreateStory(src = UiEventSourceIdentity)) }
  EventBus.EditStoryPropertiesResponses foreach { evt =>
    val editedProperties = Main.storyPropertiesEditor(evt.story).showAndWait()

    editedProperties foreach { case (title, author, desc, metadata) =>
      EventBus.send(UpdateStoryProperties(title, author, desc, metadata, UiEventSourceIdentity))
    }
  }

  EventBus.UiNodeSelectedEvents foreach { evt => selectedNode = Some(evt.id) }
  EventBus.UiNodeDeselectedEvents foreach { _ => selectedNode = None }

  EventBus.FactCreatedEvents foreach { evt => FactViewer.add(evt.fact) }
  EventBus.FactUpdatedEvents foreach { evt => FactViewer.update(evt.fact, evt.updatedFact) }
  EventBus.FactDestroyedEvents foreach { evt => FactViewer.remove(evt.fact) }

  def requestNewNode(): Unit = {
    EventBus.send(NewNodeRequest(UiEventSourceIdentity))
  }
  def requestEditNode(): Unit = {
    selectedNode foreach { id => EventBus.send(EditNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestDeleteNode(): Unit = {
    selectedNode foreach { id => EventBus.send(DeleteNodeRequest(id, UiEventSourceIdentity)) }
  }

  def requestNewFact(): Unit = {
    EventBus.send(NewFactRequest(UiEventSourceIdentity))
  }
  def requestEditFact(): Unit = {
    Option(FactViewer.selectionModel.selectedItem()) foreach { f =>
      EventBus.send(EditFactRequest(f.id, UiEventSourceIdentity))
    }
  }
  def requestDeleteFact(): Unit = {
    Option(FactViewer.selectionModel.selectedItem()) foreach { f =>
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

  def requestCut(): Unit = {
    selectedNode foreach { id => EventBus.send(CutNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestCopy(): Unit = {
    selectedNode foreach { id => EventBus.send(CopyNodeRequest(id, UiEventSourceIdentity)) }
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

}
