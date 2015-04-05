package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.dialogs.NodeEditor
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.story.Node

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{TextInputDialog, Dialog, Button, ToolBar}
import scalafx.stage.Modality

object Toolbar {
  val toolbar = new ToolBar() {
    items.addAll(newNode, deleteNode, editNode)
  }

  private lazy val newNode = new Button("New Node") {
    onAction = (ae: ActionEvent) => {
      EventBus send NewNodeRequest
    }
  }
  private lazy val deleteNode = new Button("Delete Node") {
    onAction = (ae: ActionEvent) => {
      UIEventDispatcher.selectedNodeId foreach { id => EventBus send DeleteNodeRequest(id) }
    }
  }
  private lazy val editNode = new Button("Edit Node") {
    onAction = (ae: ActionEvent) => {
      UIEventDispatcher.selectedNodeId foreach { id => EventBus send EditNodeRequest(id) }
    }
  }
}
