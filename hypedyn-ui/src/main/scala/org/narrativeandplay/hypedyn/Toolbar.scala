package org.narrativeandplay.hypedyn

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
      val nodeName = new TextInputDialog() {
        title = "New Node"
        headerText = None
        contentText = "Enter the node's name:"

        initModality(Modality.APPLICATION_MODAL)
      }.showAndWait()

      nodeName foreach { n =>
        EventBus send CreateNode(new Node {
          override def name: String = n

          override def content: String = ""

          override def id: Long = 0
        })
      }
    }
  }
  private lazy val deleteNode = new Button("Delete Node")
  private lazy val editNode = new Button("Edit Node")
}
