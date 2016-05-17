package org.narrativeandplay.hypedyn.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Skin, Control => JfxControl}
import javafx.scene.{input => jfxsi}

import org.narrativeandplay.hypedyn.story.rules.RuleLike

import scala.collection.mutable.ArrayBuffer
import scalafx.Includes._
import scalafx.event.Event
import scalafx.geometry.Point2D
import scalafx.scene.input.{MouseEvent, ScrollEvent}
import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThemeLike}
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal}
import org.narrativeandplay.hypedyn.storyviewer.components.{LinkGroup, ViewerMotif, ViewerNode, ViewerTheme}
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._

/**
 * Content pane for the story viewer
 *
 * @param pluginEventDispatcher The event dispatcher for the story viewer, that is allowed to send events
 */
class StoryViewerContent(private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]
  val themes = ArrayBuffer.empty[ViewerTheme]
  val motifs = ArrayBuffer.empty[ViewerMotif]

  skin = new StoryViewerContentSkin(this)

  addEventFilter(MouseEvent.MousePressed, { event: jfxsi.MouseEvent =>
    val pt = new Point2D(event.getX, event.getY)

    nodes foreach { n => if (!(n.bounds contains pt)) n.deselect() }
    themes foreach { t => if (!(t.bounds contains pt)) t.deselect() }
    motifs foreach { m => if (!(m.bounds contains pt)) m.deselect() }
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
   * Remove all nodes and links and themes
   */
  def clear(): Unit = {
    nodes foreach (children -= _)
    themes foreach (children -= _)
    motifs foreach (children -= _)

    linkGroups.clear()
    nodes.clear()
    themes.clear()
    motifs.clear()
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
    * Add a theme
    *
    * @param theme The data of the theme to add
    * @return The added theme
    */
  def addTheme(theme: ThemeLike): ViewerTheme = {
    val t = makeTheme(theme)

    //makeAllLinks(n)

    t
  }

  /**
    * Update a theme
    * @param theme The data of the theme to update
    * @param updatedTheme The updated data of the theme
    */
  def updateTheme(theme: ThemeLike, updatedTheme: ThemeLike): Unit = {
    val viewerThemeOption = themes find (_.id == theme.id)

    viewerThemeOption foreach { viewerTheme =>
      viewerTheme.theme = updatedTheme
    }

    requestLayout()
  }

  /**
    * Remove a theme
    *
    * @param theme The theme to remove
    */
  def removeTheme(theme: ThemeLike): Unit = {
    val themeToRemoveOption = themes find (_.id == theme.id)

    themeToRemoveOption foreach { themeToRemove =>
      children -= themeToRemove
      themes -= themeToRemove
    }
  }

  /**
    * Add a motif
    *
    * @param motif The data of the motif to add
    * @return The added motif
    */
  def addMotif(motif: MotifLike): ViewerMotif = {
    val t = makeMotif(motif)

    //makeAllLinks(n)

    t
  }

  /**
    * Update a motif
    * @param motif The data of the motif to update
    * @param updatedMotif The updated data of the motif
    */
  def updateMotif(motif: MotifLike, updatedMotif: MotifLike): Unit = {
    val viewerMotifOption = motifs find (_.id == motif.id)

    viewerMotifOption foreach { viewerMotif =>
      viewerMotif.motif = updatedMotif
    }

    requestLayout()
  }

  /**
    * Remove a motif
    *
    * @param motif The motif to remove
    */
  def removeMotif(motif: MotifLike): Unit = {
    val motifToRemoveOption = motifs find (_.id == motif.id)

    motifToRemoveOption foreach { motifToRemove =>
      children -= motifToRemove
      motifs -= motifToRemove
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
    val ts = story.themes map makeTheme
    val ms = story.motifs map makeMotif
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

  /**
    * Creates a theme in the viewer
    *
    * @param themelike The data of the theme to create
    * @return The created theme in the viewer
    */
  private def makeTheme(themelike: ThemeLike): ViewerTheme = {
    val theme = new ViewerTheme(themelike, pluginEventDispatcher)
    themes += theme
    children += theme

    theme
  }

  /**
    * Creates a motif in the viewer
    *
    * @param motiflike The data of the motif to create
    * @return The created motif in the viewer
    */
  private def makeMotif(motiflike: MotifLike): ViewerMotif = {
    val motif = new ViewerMotif(motiflike, pluginEventDispatcher)
    motifs += motif
    children += motif

    motif
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
