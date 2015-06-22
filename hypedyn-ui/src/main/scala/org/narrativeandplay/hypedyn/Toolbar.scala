package org.narrativeandplay.hypedyn

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

import org.narrativeandplay.hypedyn.events.UiEventDispatcher

object Toolbar extends ToolBar {
  items.addAll(newNode, editNode, deleteNode, newFact, editFact, deleteFact)

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

  private lazy val newFact = new Button("New Fact") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewFact()
    }
  }
  private lazy val deleteFact = new Button("Delete Fact")
  private lazy val editFact = new Button("Edit Fact")
}
