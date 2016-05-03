package org.narrativeandplay.hypedyn.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Control => JfxControl, Skin}
import javafx.scene.{input => jfxsi}

import org.narrativeandplay.hypedyn.story.rules.RuleLike

import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.event.Event
import scalafx.geometry.Point2D
import scalafx.scene.input.{ScrollEvent, MouseEvent}

import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamValue, ParamName}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal}
import org.narrativeandplay.hypedyn.storyviewer.components.{ViewerNode, LinkGroup}
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._

/**
 * Content pane for the story viewer
 *
 * @param pluginEventDispatcher The event dispatcher for the story viewer, that is allowed to send events
 */
class StoryViewerContent(private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]

  skin = new StoryViewerContentSkin(this)

  addEventFilter(MouseEvent.MousePressed, { event: jfxsi.MouseEvent =>
    val pt = new Point2D(event.getX, event.getY)

    nodes foreach { n => if (!(n.bounds contains pt)) n.deselect() }
    links foreach { l => if (!(l contains pt)) l.deselect() }
    requestLayout()

    if (nodes forall (!_.bounds.contains(pt))) {
      links find (_ contains pt) foreach { l => if (l.selected()) l.deselect() else l.select(pt) }
    }
  })

  private def zoomValueClamp(d: Double) = pluginEventDispatcher.zoomValueClamp(d)
  addEventFilter(ScrollEvent.SCROLL, { event: jfxsi.ScrollEvent =>
    if (event.shortcutDown) {
      event.getDeltaY match {
        case x if x > 0 =>
          pluginEventDispatcher.zoomLevel() = zoomValueClamp(pluginEventDispatcher.zoomLevel() + 0.1)
        case y if y < 0 =>
          pluginEventDispatcher.zoomLevel() = zoomValueClamp(pluginEventDispatcher.zoomLevel() - 0.1)
      }
    }
  })

  /**
   * Returns all the link representations for the story
   */
  def links = (linkGroups flatMap (_.links)).toList

  /**
   * Remove all nodes and links
   */
  def clear(): Unit = {
    nodes foreach (children -= _)

    linkGroups.clear()
    nodes.clear()
  }

  /**
   * Add a node
   *
   * @param node The data of the node to add
   * @return The added node
   */
  def addNode(node: Nodal): ViewerNode = {
    val n = makeNode(node)

    makeAllLinks(n)

    n
  }

  /**
   * Update a node
   * @param node The data of the node to update
   * @param updatedNode The updated data of the node
   */
  def updateNode(node: Nodal, updatedNode: Nodal): Unit = {
    val viewerNodeOption = nodes find (_.id == node.id)

    viewerNodeOption foreach { viewerNode =>
      viewerNode.node = updatedNode

      linkGroups filter (_.endPoints contains viewerNode) foreach { grp =>
        val linksToRemove = grp.links filter (_.from == viewerNode)
        grp.removeAll(linksToRemove)
      }

      makeAllLinks(viewerNode)
    }

    requestLayout()
  }

  /**
   * Remove a node
   *
   * @param node The node to remove
   */
  def removeNode(node: Nodal): Unit = {
    val nodeToRemoveOption = nodes find (_.id == node.id)

    nodeToRemoveOption foreach { nodeToRemove =>
      children -= nodeToRemove
      linkGroups --= linkGroups filter (_.endPoints contains nodeToRemove)
      nodes -= nodeToRemove
    }
  }

  /**
   * Creates the visual representation of the loaded story
   *
   * @param story The story to load
   */
  def loadStory(story: Narrative): Unit = {
    val ns = story.nodes map makeNode
    ns foreach makeAllLinks
  }

  /**
   * Creates a node in the viewer
   *
   * @param nodal The data of the node to create
   * @return The created node in the viewer
   */
  private def makeNode(nodal: Nodal): ViewerNode = {
    val node = new ViewerNode(nodal, pluginEventDispatcher)
    nodes += node
    children += node

    node
  }

  /**
    * creates both types of links ("follow link to" and "show in popup") for a given node
    *
    * @param viewerNode The node to create links for
    *
    */
  private def makeAllLinks(viewerNode: ViewerNode): Unit = {
    makeLinks(viewerNode, viewerNode.node().links, ActionType("LinkTo"))
    makeLinks(viewerNode, viewerNode.node().showInPopups, ActionType("ShowPopupNode"))
  }

  /**
   * Creates the links for a given node
   *
   * @param viewerNode The node to create links for
   * @param links list of links
   * @param actionType type of action for these links
   */
  private def makeLinks(viewerNode: ViewerNode, links: List[RuleLike], actionType: ActionType): Unit = {
    links foreach { link =>
      val toNode = link.actions find (_.actionType == actionType) flatMap (_.params get ParamName("node")) flatMap { linkTo =>
        val nodeId = linkTo.asInstanceOf[ParamValue.Node].node
        nodes find (_.id == nodeId)
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
