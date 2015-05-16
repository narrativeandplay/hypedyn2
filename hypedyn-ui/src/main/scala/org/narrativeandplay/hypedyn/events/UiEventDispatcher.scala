package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.Main
import org.narrativeandplay.hypedyn.dialogs.NodeEditor
import org.narrativeandplay.hypedyn.story.NodeId

object UiEventDispatcher {
  val UiEventSourceIdentity = "UI"
  private var selectedNode: Option[NodeId] = None

  EventBus.NewNodeResponses foreach { _ =>
    val newNode = new NodeEditor("New Node").showAndWait()

    newNode foreach { n => EventBus.send(CreateNode(n, UiEventSourceIdentity)) }
  }
  EventBus.EditNodeResponses foreach { evt =>
    val editedNode = new NodeEditor("Edit Node", evt.node).showAndWait()

    editedNode foreach { n => EventBus.send(UpdateNode(evt.node, n, UiEventSourceIdentity)) }
  }
  EventBus.DeleteNodeResponses foreach { evt => EventBus.send(DestroyNode(evt.node, UiEventSourceIdentity)) }

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

  EventBus.NewStoryResponses foreach { _ => EventBus.send(CreateStory(src = UiEventSourceIdentity)) }

  EventBus.UiNodeSelectedEvents foreach { evt => selectedNode = Some(evt.id) }
  EventBus.UiNodeDeselectedEvents foreach { _ => selectedNode = None }

  def requestNewNode(): Unit = {
    EventBus.send(NewNodeRequest(UiEventSourceIdentity))
  }
  def requestEditNode(): Unit = {
    selectedNode foreach { id => EventBus.send(EditNodeRequest(id, UiEventSourceIdentity)) }
  }
  def requestDeleteNode(): Unit = {
    selectedNode foreach { id => EventBus.send(DeleteNodeRequest(id, UiEventSourceIdentity)) }
  }

  def requestNewStory(): Unit = {
    EventBus.send(NewStoryRequest(UiEventSourceIdentity))
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
