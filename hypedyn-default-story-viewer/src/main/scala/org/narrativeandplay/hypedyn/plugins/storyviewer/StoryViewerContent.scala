package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.{input => jfxsi}

import scala.language.implicitConversions
import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.event.Event
import scalafx.scene.input.MouseEvent

import org.narrativeandplay.hypedyn.plugins.storyviewer.components.{Link, ViewerNode, LinkGroup}
import org.narrativeandplay.hypedyn.plugins.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.{NodeId, Nodal}

class StoryViewerContent(private val eventDispatcher: StoryViewer) extends Control {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]

  setSkin(new StoryViewerContentSkin(this))

  addEventFilter(MouseEvent.MouseClicked, new EventHandler[jfxsi.MouseEvent] {
    override def handle(event: jfxsi.MouseEvent): Unit = {
      nodes foreach (_.deselect())
      links foreach (_.deselect())
      links find (_.contains(event.getX, event.getY)) foreach (_.select(event.getX, event.getY))
    }
  })

  def links: List[Link] = linkGroups.flatMap(_.links).toList

  def addNode(node: Nodal) = {
    val n = new ViewerNode(node.name, node.content.text, node.id, eventDispatcher)
    nodes += n
    children += n

    node.content.rulesets flatMap (_.rules) filter { rule =>
      rule.actions map (_.actionType) contains "LinkTo"
    } foreach { rule =>
      val toNode = rule.actions find (_.actionType == "LinkTo") flatMap (_.params get "node") flatMap { idString =>
        nodes find (_.id == NodeId(BigInt(idString)))
      }

      toNode foreach { to =>
        val linkGroup = linkGroups find (_.endPoints == UnorderedPair(n, to)) match {
          case Some(grp) => grp
          case None =>
            val grp = new LinkGroup(n, to)
            linkGroups += grp
            grp
        }

        linkGroup.insert(rule, n, to)
      }
    }

    n
  }

  def updateNode(node: Nodal, updatedNode: Nodal): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      n.name = updatedNode.name
      n.content = updatedNode.content.text

      updatedNode.content.rulesets flatMap (_.rules) filter { rule =>
        rule.actions map (_.actionType) contains "LinkTo"
      } foreach { rule =>
        val toNode = rule.actions find (_.actionType == "LinkTo") flatMap (_.params get "node") flatMap { idString =>
          nodes find (_.id == NodeId(BigInt(idString)))
        }

        toNode foreach { to =>
          val linkGroup = linkGroups find (_.endPoints == UnorderedPair(n, to)) match {
            case Some(grp) => grp
            case None =>
              val grp = new LinkGroup(n, to)
              linkGroups += grp
              grp
          }

          linkGroup.insert(rule, n, to)
        }
      }
    }
  }

  def removeNode(node: Nodal): Unit = {
    nodes find (_.id == node.id) foreach { n =>
      children -= n
      linkGroups --= linkGroups filter (_.endPoints.contains(n))
      nodes -= n
    }
  }

  def clear(): Unit = {
    children.clear()
    linkGroups.clear()
    nodes.clear()
  }

  // <editor-fold desc="Utility Methods for a Scala-like access pattern">

  def children = getChildren

  def onMouseClicked = getOnMouseClicked
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def prefWidth = getPrefWidth
  def prefWidth_=(width: Double) = setPrefWidth(width)

  def prefHeight = getPrefHeight
  def prefHeight_=(width: Double) = setPrefHeight(width)

  def width = getWidth

  def height = getHeight

  // </editor-fold>
}
