package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.{input => jfxsi}

import scalafx.Includes._
import scalafx.event.Event
import scalafx.scene.input.MouseEvent

import org.narrativeandplay.hypedyn.story.NodeLike

import scala.collection.mutable.ArrayBuffer

class StoryViewerContent extends Control {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]

  setSkin(new StoryViewerContentSkin(this))

  onMouseClicked = { me =>
    links foreach (_.deselect())
    nodes foreach (_.deselect())
    links find (_.contains(me.x, me.y)) foreach (_.select(me.x, me.y))
  }

  def links: List[Link] = linkGroups.flatMap(_.links).toList

  def addNode(node: NodeLike) = {
    val n = new ViewerNode(node.name, node.content, node.id)
    nodes += n
    children += n

    n
  }

  def updateNode(node: NodeLike): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      n.name = node.name
      n.content = node.content
    }
  }

  def removeNode(node: NodeLike): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      children -= n
      linkGroups --= linkGroups filter (_.endPoints.contains(n))
      nodes -= n
    }
  }

  // <editor-fold desc="Utility Methods for a ScalaFX-like access pattern">

  def children = getChildren

  def onMouseClicked = getOnMouseClicked

  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  // </editor-fold>
}
