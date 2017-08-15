package org.narrativeandplay.hypedyn.ui.uicomponents

import java.lang

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.ui.events.UiEventDispatcher

/**
 * The toolbar for the application
 */
object Toolbar extends ToolBar {
  items.addAll(runStory, newNode, editNode, deleteNode, newFact, editFact, deleteFact)

  private lazy val runStory = new Button("Run") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestRunStory()
    }
  }

  private lazy val newNode = new Button("New Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewNode()
    }
  }
  private lazy val deleteNode = new Button("Delete Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestDeleteNode()
    }

    disable <== EasyBind monadic UiEventDispatcher.selectedNode map[lang.Boolean] (_.isEmpty)
  }
  private lazy val editNode = new Button("Edit Node") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestEditNode()
    }

    disable <== EasyBind monadic UiEventDispatcher.selectedNode map[lang.Boolean] (_.isEmpty)
  }

  private lazy val newFact = new Button("New Fact") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewFact()
    }
  }
  private lazy val deleteFact = new Button("Delete Fact") {
    onAction = { _ =>
      UiEventDispatcher.requestDeleteFact()
    }

    disable <== FactViewer.selectionModel().selectedItem.isNull
  }
  private lazy val editFact = new Button("Edit Fact") {
    onAction = { _ =>
      UiEventDispatcher.requestEditFact()
    }

    disable <== FactViewer.selectionModel().selectedItem.isNull
  }
}
