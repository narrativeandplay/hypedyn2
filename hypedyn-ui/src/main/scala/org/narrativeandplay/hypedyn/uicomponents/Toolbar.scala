package org.narrativeandplay.hypedyn.uicomponents

import java.lang

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.events.UiEventDispatcher

/**
 * The toolbar for the application
 */
object Toolbar extends ToolBar {
  items.addAll(runStory, newNode, editNode, deleteNode, newFact, editFact, deleteFact,
    newTheme, editTheme, deleteTheme, newMotif, editMotif, deleteMotif)

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

  private lazy val newTheme = new Button("New Theme") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewTheme()
    }
  }
  private lazy val deleteTheme = new Button("Delete Theme") {
    onAction = { _ =>
      UiEventDispatcher.requestDeleteTheme()
    }

    //disable <== FactViewer.selectionModel().selectedItem.isNull
  }
  private lazy val editTheme = new Button("Edit Theme") {
    onAction = { _ =>
      UiEventDispatcher.requestEditTheme()
    }

    //disable <== FactViewer.selectionModel().selectedItem.isNull
  }

  private lazy val newMotif = new Button("New Motif") {
    onAction = { actionEvent: ActionEvent =>
      UiEventDispatcher.requestNewMotif()
    }
  }
  private lazy val deleteMotif = new Button("Delete Motif") {
    onAction = { _ =>
      UiEventDispatcher.requestDeleteMotif()
    }

    //disable <== FactViewer.selectionModel().selectedItem.isNull
  }
  private lazy val editMotif = new Button("Edit Motif") {
    onAction = { _ =>
      UiEventDispatcher.requestEditMotif()
    }

    //disable <== FactViewer.selectionModel().selectedItem.isNull
  }
}
