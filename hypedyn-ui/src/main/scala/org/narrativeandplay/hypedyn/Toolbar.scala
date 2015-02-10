package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.events.{NewNodeEvent, EventBus}

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ToolBar}

object Toolbar {
  val toolbar = new ToolBar() {
    items.addAll(newNode, deleteNode, editNode)
  }

  private lazy val newNode = new Button("New Node") {
    onAction = (ae: ActionEvent) => EventBus.send(NewNodeEvent)
  }
  private lazy val deleteNode = new Button("Delete Node")
  private lazy val editNode = new Button("Edit Node")
}
