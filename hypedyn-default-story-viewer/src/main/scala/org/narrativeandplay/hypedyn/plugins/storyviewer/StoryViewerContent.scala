package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.input.MouseEvent

import org.narrativeandplay.hypedyn.story.Node

import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._

import utils.SAMConversions._

class StoryViewerContent extends Control {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]

  setSkin(new StoryViewerContentSkin(this))

  onMouseClicked = { (me: MouseEvent) =>
    links foreach (_.deselect())
    links find (_.contains(me.getX, me.getY)) foreach (_.select(me.getX, me.getY))
  }

  def links: List[Link] = linkGroups.flatMap(_.links).toList

  def addNode(node: Node) = {
    val n = new ViewerNode(node.name, node.content, node.id)
    nodes += n
    children += n

    n
  }

  def updateNode(node: Node): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      n.name = node.name
      n.content = node.content
    }
  }

  def removeNode(node: Node): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      children -= n
      linkGroups --= linkGroups filter (_.endPoints.contains(n))
      nodes -= n
    }
  }

  // <editor-fold desc="Utility Methods for a ScalaFX-like access pattern">

  def children = getChildren

  def onMouseClicked = getOnMouseClicked

  def onMouseClicked_=(value: EventHandler[_ >: MouseEvent]) = setOnMouseClicked(value)

  // </editor-fold>
}
