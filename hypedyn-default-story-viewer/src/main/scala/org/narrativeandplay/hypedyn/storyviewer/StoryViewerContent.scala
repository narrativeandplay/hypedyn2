package org.narrativeandplay.hypedyn.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Control => JfxControl, Skin}
import javafx.scene.{input => jfxsi}

import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.event.Event
import scalafx.scene.input.MouseEvent

import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamName
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.{NodeId, Narrative, Nodal}
import org.narrativeandplay.hypedyn.storyviewer.components.{ViewerNode, LinkGroup}
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._

class StoryViewerContent(private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]

  skin = new StoryViewerContentSkin(this)

  addEventFilter(MouseEvent.MouseClicked, { event: jfxsi.MouseEvent =>
    nodes foreach (_.deselect())
    links foreach (_.deselect())
    links find (_.contains(event.getX, event.getY)) foreach (_.select(event.getX, event.getY))
  })

  def links = (linkGroups flatMap (_.links)).toList

  def clear(): Unit = {
    children.clear()
    linkGroups.clear()
    nodes.clear()
  }

  def addNode(node: Nodal): Unit = {
    val n = makeNode(node)

    makeLinks(n)
  }

  def updateNode(node: Nodal, updatedNode: Nodal): Unit = {
    val viewerNodeOption = nodes find (_.id == node.id)

    viewerNodeOption foreach { viewerNode =>
      viewerNode.node = updatedNode

      linkGroups filter (_.endPoints contains viewerNode) foreach { grp =>
        val linksToRemove = grp.links filter (_.from == viewerNode)
        grp.removeAll(linksToRemove)
      }

      makeLinks(viewerNode)
    }

    requestLayout()
  }

  def removeNode(node: Nodal): Unit = {
    val nodeToRemoveOption = nodes find (_.id == node.id)

    nodeToRemoveOption foreach { nodeToRemove =>
      children -= nodeToRemove
      linkGroups --= linkGroups filter (_.endPoints contains nodeToRemove)
      nodes -= nodeToRemove
    }
  }

  def loadStory(story: Narrative): Unit = {
    val ns = story.nodes map makeNode
    ns foreach makeLinks
  }

  private def makeNode(nodal: Nodal): ViewerNode = {
    val node = new ViewerNode(nodal, pluginEventDispatcher)
    nodes += node
    children += node

    node
  }

  private def makeLinks(viewerNode: ViewerNode): Unit = {
    viewerNode.node().links foreach { link =>
      val toNode = link.actions find (_.actionType == ActionType("LinkTo")) flatMap (_.params get ParamName("node")) flatMap { idVal =>
        nodes find (_.id == NodeId(BigInt(idVal.value)))
      }

      toNode foreach { to =>
        val linkGroup = linkGroups find (_.endPoints == UnorderedPair(viewerNode, to)) match {
          case Some(grp) => grp
          case None =>
            val grp = new LinkGroup(viewerNode, to)
            linkGroups += grp
            grp
        }

        linkGroup.insert(viewerNode, to, link)
      }
    }
  }

  // <editor-fold desc="Utility Methods for a Scala-like access pattern">

  def children = getChildren

  def onMouseClicked = { me: MouseEvent => getOnMouseClicked.handle(me) }
  def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
    setOnMouseClicked(new EventHandler[U] {
      override def handle(event: U): Unit = lambda(event)
    })
  }

  def prefWidth = getPrefWidth
  def prefWidth_=(width: Double) = setPrefWidth(width)

  def prefHeight = getPrefHeight
  def prefHeight_=(width: Double) = setPrefHeight(width)
  
  def skin = getSkin
  def skin_=(s: Skin[_]) = setSkin(s)

  def width = getWidth

  def height = getHeight

  // </editor-fold>
}
