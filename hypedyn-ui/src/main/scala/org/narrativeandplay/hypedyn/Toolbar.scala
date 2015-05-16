package org.narrativeandplay.hypedyn

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

import org.narrativeandplay.hypedyn.events.UiEventDispatcher

object Toolbar extends ToolBar {
  items.addAll(newNode, editNode, deleteNode)

  private lazy val newNode = new Button("New Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewNode()
    }
  }
  private lazy val deleteNode = new Button("Delete Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestDeleteNode()
    }
  }
  private lazy val editNode = new Button("Edit Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestEditNode()
    }
  }
}
