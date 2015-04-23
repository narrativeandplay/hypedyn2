package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.events._

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

object Toolbar {
  val toolbar = new ToolBar() {
    items.addAll(newNode, deleteNode, editNode)
  }

  private lazy val newNode = new Button("New Node") {
    onAction = { actionEvent: ActionEvent =>
      EventBus send NewNodeRequest
    }
  }
  private lazy val deleteNode = new Button("Delete Node") {
    onAction = { actionEvent: ActionEvent =>
      UIEventDispatcher.selectedNodeId foreach { id => EventBus send DeleteNodeRequest(id) }
    }
  }
  private lazy val editNode = new Button("Edit Node") {
    onAction = { actionEvent: ActionEvent =>
      UIEventDispatcher.selectedNodeId foreach { id => EventBus send EditNodeRequest(id) }
    }
  }
}
