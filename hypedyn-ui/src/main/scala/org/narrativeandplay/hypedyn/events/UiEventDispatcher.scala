package org.narrativeandplay.hypedyn.events

import java.io.File

import scala.language.reflectiveCalls

import scalafx.Includes._
import scalafx.stage.{Window, Stage, FileChooser}
import scalafx.stage.FileChooser.ExtensionFilter

import org.narrativeandplay.hypedyn.dialogs.NodeEditor

object UiEventDispatcher {
  var mainStage: Stage = _
  val saveLoadFileChooser = new FileChooser() {
    extensionFilters += new ExtensionFilter("HypeDyn Story", "*.dyn")

    def showOpenFileDialog(ownerWindow: Window): Option[File] = Option(showOpenDialog(ownerWindow))

    def showSaveFileDialog(ownerWindow: Window): Option[File] = Option(showSaveDialog(ownerWindow))
  }


  var selectedNodeId: Option[Long] = None

  EventBus.newNodeEvents subscribe { evt =>
    val newNode = new NodeEditor("New Node").showAndWait()

    newNode foreach (EventBus send CreateNode(_))
  }

  EventBus.editNodeEvents subscribe { evt =>
    val editedNode = new NodeEditor("Edit Node", evt.node).showAndWait()

    editedNode foreach (EventBus send UpdateNode(evt.node, _))
  }

  EventBus.nodeSelectedEvents subscribe { evt => selectedNodeId = Some(evt.nodeId) }
  EventBus.nodeDeselectedEvents subscribe { evt => selectedNodeId = None }

  def save(): Unit = {
    val saveFile = saveLoadFileChooser.showSaveFileDialog(mainStage)

    saveFile foreach (EventBus send SaveEvent(_))
  }

  def load(): Unit = {
    val saveFile = saveLoadFileChooser.showOpenFileDialog(mainStage)

    saveFile foreach (EventBus send LoadEvent(_))
  }
}
