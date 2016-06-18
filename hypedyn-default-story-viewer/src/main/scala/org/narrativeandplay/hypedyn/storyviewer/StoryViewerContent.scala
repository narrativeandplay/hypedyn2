package org.narrativeandplay.hypedyn.storyviewer

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{Skin, Control => JfxControl}
import javafx.scene.{input => jfxsi}

import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.story.rules.RuleLike

import scala.collection.mutable.ArrayBuffer
import scalafx.Includes._
import scalafx.event.Event
import scalafx.geometry.Point2D
import scalafx.scene.input.{MouseEvent, ScrollEvent}
import org.narrativeandplay.hypedyn.storyviewer.utils.UnorderedPair
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID, ThemeLike}
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.storyviewer.components._
import org.narrativeandplay.hypedyn.storyviewer.utils.ViewerConversions._

/**
 * Content pane for the story viewer
 *
 * @param pluginEventDispatcher The event dispatcher for the story viewer, that is allowed to send events
 */
class StoryViewerContent(private val pluginEventDispatcher: StoryViewer) extends JfxControl {
  val nodes = ArrayBuffer.empty[ViewerNode]
  val linkGroups = ArrayBuffer.empty[LinkGroup]
  val thematicLinkGroups = ArrayBuffer.empty[ThematicLinkGroup]
  val themes = ArrayBuffer.empty[ViewerTheme]
  val motifs = ArrayBuffer.empty[ViewerMotif]
  val themesubthemelinkGroups = ArrayBuffer.empty[ThemeSubthemeLinkGroup]
  val thememotiflinkGroups = ArrayBuffer.empty[ThemeMotifLinkGroup]

  skin = new StoryViewerContentSkin(this)

  addEventFilter(MouseEvent.MousePressed, { event: jfxsi.MouseEvent =>
    val pt = new Point2D(event.getX, event.getY)

    nodes foreach { n => if (!(n.bounds contains pt)) n.deselect() }
    themes foreach { t => if (!(t.bounds contains pt)) t.deselect() }
    motifs foreach { m => if (!(m.bounds contains pt)) m.deselect() }
    links foreach { l => if (!(l contains pt)) l.deselect() }
    thematiclinks foreach { l => if (!(l contains pt)) l.deselect() }
    themesubthemelinks foreach { l => if (!(l contains pt)) l.deselect() }
    thememotiflinks foreach { l => if (!(l contains pt)) l.deselect() }
    requestLayout()
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
    * Returns all the thematic link representations for the story
    */
  def thematiclinks = (thematicLinkGroups flatMap (_.links)).toList

  /**
    * Returns all the theme-subtheme link representations for the story
    */
  def themesubthemelinks = (themesubthemelinkGroups flatMap (_.links)).toList

  /**
    * Returns all the theme-motif link representations for the story
    */
  def thememotiflinks = (thememotiflinkGroups flatMap (_.links)).toList

  /**
   * Remove all nodes and links and themes
   */
  def clear(): Unit = {
    nodes foreach (children -= _)
    themes foreach (children -= _)
    motifs foreach (children -= _)

    linkGroups.clear()
    thematicLinkGroups.clear()
    themesubthemelinkGroups.clear()
    thememotiflinkGroups.clear()
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
    *
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

      thematicLinkGroups filter (_.endPoints contains viewerNode) foreach { grp =>
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
      thematicLinkGroups --= thematicLinkGroups filter (_.endPoints contains nodeToRemove)
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

    makeAllLinks(t)
    refreshThematicLinks()

    t
  }

  /**
    * Update a theme
    *
    * @param theme The data of the theme to update
    * @param updatedTheme The updated data of the theme
    */
  def updateTheme(theme: ThemeLike, updatedTheme: ThemeLike): Unit = {
    val viewerThemeOption = themes find (_.id == theme.id)

    viewerThemeOption foreach { viewerTheme =>
      viewerTheme.theme = updatedTheme

      themesubthemelinkGroups filter (_.endPoints contains viewerTheme) foreach { grp =>
        val linksToRemove = grp.links filter (_.to == viewerTheme)
        grp.removeAll(linksToRemove)
      }
      thememotiflinkGroups filter (_.thetheme == viewerTheme) foreach { grp =>
        val linksToRemove = grp.links filter (_.to == viewerTheme)
        grp.removeAll(linksToRemove)
      }

      makeAllLinks(viewerTheme)
    }
    refreshThematicLinks()

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
      themesubthemelinkGroups --= themesubthemelinkGroups filter (_.endPoints contains themeToRemove)
      thememotiflinkGroups --= thememotiflinkGroups filter (_.thetheme == themeToRemove)
      themes -= themeToRemove
    }
    refreshThematicLinks()
  }

  /**
    * Add a motif
    *
    * @param motif The data of the motif to add
    * @return The added motif
    */
  def addMotif(motif: MotifLike): ViewerMotif = {
    val m = makeMotif(motif)
    refreshThematicLinks()

    m
  }

  /**
    * Update a motif
    *
    * @param motif The data of the motif to update
    * @param updatedMotif The updated data of the motif
    */
  def updateMotif(motif: MotifLike, updatedMotif: MotifLike): Unit = {
    val viewerMotifOption = motifs find (_.id == motif.id)

    viewerMotifOption foreach { viewerMotif =>
      viewerMotif.motif = updatedMotif

    }
    refreshThematicLinks()

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
      thememotiflinkGroups --= thememotiflinkGroups filter (_.themotif == motifToRemove)
      motifs -= motifToRemove
    }
    refreshThematicLinks()
  }

  /**
   * Creates the visual representation of the loaded story
   *
   * @param story The story to load
   */
  def loadStory(story: Narrative): Unit = {
    val ns = story.nodes map makeNode
    val ts = story.themes map makeTheme
    val ms = story.motifs map makeMotif
    ns foreach makeAllLinks
    ts foreach makeAllLinks
  }

  /**
    * Refresh the thematic links
    */
  def refreshThematicLinks(): Unit = {
    thematicLinkGroups.clear()
    nodes foreach { viewerNode =>
      if(viewerNode.node().rules exists(_.actions map (_.actionType) contains ActionType("EnableThematicLinkToHere")))
        makeThematicLinks(viewerNode)
    }
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
    if(viewerNode.node().rules exists(_.actions map (_.actionType) contains ActionType("EnableThematicLinkToHere")))
      makeThematicLinks(viewerNode)
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
    * Create the thematic links from this node to other nodes
    * Need to do this properly with events rather than call directly
    *
    * @param viewerNode the node for which to get the recommendation
    */
  private def makeThematicLinks(viewerNode: ViewerNode): Unit = {
    pluginEventDispatcher.requestRecommendation(viewerNode.node().id)
  }

  def onRecommendationResponse(nodeId: NodeId, recommendation: List[(Nodal, Double)]): Unit = {
    Logger.info("Got recommended node for " + nodeId)
    recommendation foreach { thisRecommendation =>
      Logger.info("recommendation: node: " + thisRecommendation._1.name +
        ", score: " + thisRecommendation._2)
    }

    val viewerNodeOption = nodes find (_.id == nodeId)

    viewerNodeOption foreach { viewerNode =>
      recommendation filterNot(_._1.id == nodeId) foreach { thisRecommendation =>
        val toNodeOption = nodes find (_.id == thisRecommendation._1.id)
        toNodeOption foreach { toNode =>

          val linkGroup = thematicLinkGroups find (_.endPoints == UnorderedPair(viewerNode, toNode)) match {
            case Some(grp) => grp
            case None =>
              val grp = new ThematicLinkGroup(viewerNode, toNode)
              thematicLinkGroups += grp
              grp
          }

          linkGroup.insert(viewerNode, toNode)
        }
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
    * creates "connoted" links for a given theme (both subthemes and motifs)
    *
    * @param viewerTheme The theme to create links for
    *
    */
  private def makeAllLinks(viewerTheme: ViewerTheme): Unit = {
    makeSubthemeLinks(viewerTheme, viewerTheme.theme().subthemes)
    makeMotifLinks(viewerTheme, viewerTheme.theme().motifs)
  }

  /**
    * Creates the links for a given theme
    *
    * @param viewerTheme The theme to create links for
    * @param subthemes list of subthemes
    */
  private def makeSubthemeLinks(viewerTheme: ViewerTheme, subthemes: List[ThematicElementID]): Unit = {
    subthemes foreach { subthemeID =>
      val subtheme = themes find (_.id == subthemeID)

      subtheme foreach { to =>
        val linkGroup = themesubthemelinkGroups find (_.endPoints == UnorderedPair(to, viewerTheme)) match {
          case Some(grp) => grp
          case None =>
            val grp = new ThemeSubthemeLinkGroup(to, viewerTheme)
            themesubthemelinkGroups += grp
            grp
        }

        linkGroup.insert(to, viewerTheme)
      }
    }
  }

  private def makeMotifLinks(viewerTheme: ViewerTheme, themotifs: List[ThematicElementID]): Unit = {
    themotifs foreach { motifId =>
      val motif = motifs find (_.id == motifId)

      motif foreach { to =>
        val linkGroup = thememotiflinkGroups find (_.themotif == to) find (_.thetheme == viewerTheme) match {
          case Some(grp) => grp
          case None =>
            val grp = new ThemeMotifLinkGroup(to, viewerTheme)
            thememotiflinkGroups += grp
            grp
        }

        linkGroup.insert(to, viewerTheme)
      }
    }
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
