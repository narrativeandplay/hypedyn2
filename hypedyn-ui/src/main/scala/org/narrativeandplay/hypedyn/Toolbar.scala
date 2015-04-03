package org.narrativeandplay.hypedyn

import org.narrativeandplay.hypedyn.dialogs.NodeEditor
import org.narrativeandplay.hypedyn.events.{CreateNode, NewNode, EventBus}
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
      val newNode = new NodeEditor("New Node").showAndWait()

      newNode foreach { node => EventBus send CreateNode(node) }
    }
  }
  private lazy val deleteNode = new Button("Delete Node")
  private lazy val editNode = new Button("Edit Node")
}
