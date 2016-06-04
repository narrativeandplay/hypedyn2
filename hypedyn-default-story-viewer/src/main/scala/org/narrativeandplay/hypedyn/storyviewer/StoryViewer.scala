package org.narrativeandplay.hypedyn.storyviewer

import javafx.scene.{input => jfxsi}

import scala.collection.mutable
import scala.util.Try
import scalafx.Includes._
import scalafx.beans.property.DoubleProperty
import scalafx.scene.control.{Control, ScrollPane}
import scalafx.scene.input.{KeyCode, KeyEvent}
import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.plugins.{Plugin, Saveable}
import org.narrativeandplay.hypedyn.plugins.narrativeviewer.NarrativeViewer
import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID, ThemeLike}
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.storyviewer.components.{ViewerMotif, ViewerNode, ViewerTheme}
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils
import org.narrativeandplay.hypedyn.undo.{MotifMovedChange, NodeMovedChange, ThemeMovedChange, UndoableStream}

/**
 * StoryViewer implementation class
 */
class StoryViewer extends ScrollPane with Plugin with NarrativeViewer with Saveable {
  prefWidth = 800
  prefHeight = 600

  fitToHeight = true
  fitToWidth = true

  val minZoom = 0.1
  val maxZoom = 2.0
  val showContentLimit = 0.45
  val showLabelsLimit = 0.15

  val nodeLocations = mutable.Map.empty[NodeId, Vector2[Double]]
  val themeLocations = mutable.Map.empty[ThematicElementID, Vector2[Double]]
  val motifLocations = mutable.Map.empty[ThematicElementID, Vector2[Double]]
  val zoomLevel = DoubleProperty(1.0)

  val StoryViewerEventSourceIdentity = s"Plugin - $name"
  val viewer = new StoryViewerContent(this)

  content = new Control(viewer) {}

  private[storyviewer] def zoomValueClamp(v: Double) = DoubleUtils.clamp(minZoom, maxZoom, v)
  // Because OS X does something stupid by firing multiple events for a single Equals key press, we add a timestamp
  // to track when the last time the zoom was triggered, and allow it to zoom only if it was at least 2 ms after
  // the last zoom time.
  private var lastKeypressTime = System.currentTimeMillis()
  addEventFilter(KeyEvent.KeyPressed, { event: jfxsi.KeyEvent =>
    val timeDiff = System.currentTimeMillis() - lastKeypressTime
    if (event.shortcutDown && timeDiff > 1) {
      event.code match {
        case KeyCode.ADD | KeyCode.EQUALS => zoomLevel() = zoomValueClamp(zoomLevel() + 0.1)
        case KeyCode.MINUS | KeyCode.SUBTRACT => zoomLevel() = zoomValueClamp(zoomLevel() - 0.1)
        case KeyCode.NUMPAD0 | KeyCode.DIGIT0 => zoomLevel() = 1.0
        case _ =>
      }
    }
    lastKeypressTime = System.currentTimeMillis()
  })

  /**
   * Returns the name of the plugin
   */
  override def name: String = "Default Story Viewer"

  /**
   * Returns the version of the plugin
   */
  override def version: String = "1.0.0"

  /**
   * Defines what to do when a story is loaded
   *
   * @param story The story that is loaded
   */
  override def onStoryLoaded(story: Narrative): Unit = {
    viewer.clear()
    viewer.loadStory(story)
  }

  /**
   * Defines what to do when a node is created
   *
   * @param node The created node
   */
  override def onNodeCreated(node: Nodal): Unit = {
    val createdNode = viewer.addNode(node)

    nodeLocations get createdNode.id foreach (moveNode(createdNode.id, _))
  }

  /**
   * Defines what to do when a node is updated
   *
   * @param node The node to be updated
   * @param updatedNode The same node with the updates already applied
   */
  override def onNodeUpdated(node: Nodal, updatedNode: Nodal): Unit = viewer.updateNode(node, updatedNode)

  /**
   * Defines what to do when a node is destroyed
   *
   * @param node The node to be destroyed
   */
  override def onNodeDestroyed(node: Nodal): Unit = viewer.removeNode(node)

  /**
    * Defines what to do when a theme is created
    *
    * @param theme The created theme
    */
  override def onThemeCreated(theme: ThemeLike): Unit = {
    val createdTheme = viewer.addTheme(theme)

    themeLocations get createdTheme.id foreach (moveTheme(createdTheme.id, _))
  }

  /**
    * Defines what to do when a theme is updated
    *
    * @param theme The theme to be updated
    * @param updatedTheme The same theme with the updates already applied
    */
  override def onThemeUpdated(theme: ThemeLike, updatedTheme: ThemeLike): Unit = viewer.updateTheme(theme, updatedTheme)

  /**
    * Defines what to do when a theme is destroyed
    *
    * @param theme The theme to be destroyed
    */
  override def onThemeDestroyed(theme: ThemeLike): Unit = viewer.removeTheme(theme)

  /**
    * Defines what to do when a motif is created
    *
    * @param motif The created motif
    */
  override def onMotifCreated(motif: MotifLike): Unit = {
    val createdMotif = viewer.addMotif(motif)

    motifLocations get createdMotif.id foreach (moveTheme(createdMotif.id, _))
  }

  /**
    * Defines what to do when a motif is updated
    *
    * @param motif The motif to be updated
    * @param updatedMotif The same motif with the updates already applied
    */
  override def onMotifUpdated(motif: MotifLike, updatedMotif: MotifLike): Unit = viewer.updateMotif(motif, updatedMotif)

  /**
    * Defines what to do when a motif is destroyed
    *
    * @param motif The motif to be destroyed
    */
  override def onMotifDestroyed(motif: MotifLike): Unit = viewer.removeMotif(motif)

  /**
   * Restore the state of this Saveable that was saved
   *
   * @param data The saved data
   */
  override def onLoad(data: AstElement): Unit = {
    zoomLevel() = 1.0

    val properData = data.asInstanceOf[AstMap]
    val nodes = properData("nodes").asInstanceOf[AstList].elems
    nodes foreach { n =>
      val nodeData = n.asInstanceOf[AstMap]
      val (id, x, y) = deserialiseNode(nodeData)

      moveNode(id, Vector2(x, y))
    }
    val themes = properData("themes").asInstanceOf[AstList].elems
    themes foreach { t =>
      val themeData = t.asInstanceOf[AstMap]
      val (id, x, y) = deserialiseTheme(themeData)

      moveTheme(id, Vector2(x, y))
    }
    val motifs = properData("motifs").asInstanceOf[AstList].elems
    motifs foreach { t =>
      val motifData = t.asInstanceOf[AstMap]
      val (id, x, y) = deserialiseMotif(motifData)

      moveMotif(id, Vector2(x, y))
    }

    zoomLevel() = properData get "zoomLevel" map (_.asInstanceOf[AstFloat].f) getOrElse 1.0

    sizeToChildren()
  }

  /**
   * Returns the data that this Saveable would like saved
   */
  override def onSave(): AstElement = AstMap("zoomLevel" -> AstFloat(zoomLevel()),
                                             "nodes" -> AstList(viewer.nodes.toList map serialiseNode: _*),
                                             "themes" -> AstList(viewer.themes.toList map serialiseTheme: _*),
                                             "motifs" -> AstList(viewer.motifs.toList map serialiseMotif: _*))

  /**
   * Resizes the content control to ensure all nodes are shown
   */
  def sizeToChildren(): Unit = {
    val allBounds = (viewer.nodes map (_.bounds)).toList ::: (viewer.themes map (_.bounds)).toList ::: (viewer.motifs map (_.bounds)).toList
    val maxX = Try((allBounds map (_.maxX)).max) getOrElse 0d
    val maxY = Try((allBounds map (_.maxY)).max) getOrElse 0d

    if (maxX > viewportBounds().getWidth) { fitToWidth = false; viewer.prefWidth = maxX } else fitToWidth = true
    if (maxY > viewportBounds().getHeight) { fitToHeight = false; viewer.prefHeight = maxY } else fitToHeight = true
  }

  /**
   * Moves a node
   *
   * @param nodeId The ID of the node to move
   * @param position The position to move it to
   */
  def moveNode(nodeId: NodeId, position: Vector2[Double]): Unit = {
    nodeLocations += nodeId -> position

    viewer.nodes find (_.id == nodeId) foreach (_.relocate(position.x, position.y))

    sizeToChildren()
  }

  def requestNodeEdit(id: NodeId): Unit = {
    EventBus.send(EditNodeRequest(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeMove(id: NodeId, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    nodeLocations += id -> finalPos

    UndoableStream.send(new NodeMovedChange(this, id, initialPos, finalPos))
  }

  def notifyNodeSelection(id: NodeId): Unit = {
    EventBus.send(UiNodeSelected(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeDeselection(id: NodeId): Unit = {
    EventBus.send(UiNodeDeselected(id, StoryViewerEventSourceIdentity))
  }

  private def serialiseNode(n: ViewerNode) = {
    val unscaledX = n.layoutX / zoomLevel()
    val unscaledY = n.layoutY / zoomLevel()

    AstMap(
      "id" -> AstInteger(n.id.value),
      "x" -> AstFloat(unscaledX),
      "y" -> AstFloat(unscaledY)
    )
  }

  private def deserialiseNode(nodeData: AstMap) = {
    val id = nodeData("id").asInstanceOf[AstInteger].i
    val x = nodeData("x").asInstanceOf[AstFloat].f
    val y = nodeData("y").asInstanceOf[AstFloat].f

    (NodeId(id), x, y)
  }

  // note: much of this is a direct copy of above, should refactor out common code

  /**
    * Moves a theme
    *
    * @param themeID The ID of the theme to move
    * @param position The position to move it to
    */
  def moveTheme(themeID: ThematicElementID, position: Vector2[Double]): Unit = {
    themeLocations += themeID -> position

    viewer.themes find (_.id == themeID) foreach (_.relocate(position.x, position.y))

    sizeToChildren()
  }

  def requestThemeEdit(id: ThematicElementID): Unit = {
    EventBus.send(EditThemeRequest(id, StoryViewerEventSourceIdentity))
  }

  def notifyThemeMove(id: ThematicElementID, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    themeLocations += id -> finalPos

    UndoableStream.send(new ThemeMovedChange(this, id, initialPos, finalPos))
  }

  def notifyThemeSelection(id: ThematicElementID): Unit = {
    EventBus.send(UiThemeSelected(id, StoryViewerEventSourceIdentity))
  }

  def notifyThemeDeselection(id: ThematicElementID): Unit = {
    EventBus.send(UiThemeDeselected(id, StoryViewerEventSourceIdentity))
  }

  private def serialiseTheme(t: ViewerTheme) = {
    val unscaledX = t.layoutX / zoomLevel()
    val unscaledY = t.layoutY / zoomLevel()

    AstMap(
      "id" -> AstInteger(t.id.value),
      "x" -> AstFloat(unscaledX),
      "y" -> AstFloat(unscaledY)
    )
  }

  private def deserialiseTheme(themeData: AstMap) = {
    val id = themeData("id").asInstanceOf[AstInteger].i
    val x = themeData("x").asInstanceOf[AstFloat].f
    val y = themeData("y").asInstanceOf[AstFloat].f

    (ThematicElementID(id), x, y)
  }

  /**
    * Moves a motif
    *
    * @param motifID The ID of the motif to move
    * @param position The position to move it to
    */
  def moveMotif(motifID: ThematicElementID, position: Vector2[Double]): Unit = {
    motifLocations += motifID -> position

    viewer.motifs find (_.id == motifID) foreach (_.relocate(position.x, position.y))

    sizeToChildren()
  }

  def requestMotifEdit(id: ThematicElementID): Unit = {
    EventBus.send(EditMotifRequest(id, StoryViewerEventSourceIdentity))
  }

  def notifyMotifMove(id: ThematicElementID, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    motifLocations += id -> finalPos

    UndoableStream.send(new MotifMovedChange(this, id, initialPos, finalPos))
  }

  def notifyMotifSelection(id: ThematicElementID): Unit = {
    EventBus.send(UiMotifSelected(id, StoryViewerEventSourceIdentity))
  }

  def notifyMotifDeselection(id: ThematicElementID): Unit = {
    EventBus.send(UiMotifDeselected(id, StoryViewerEventSourceIdentity))
  }

  private def serialiseMotif(m: ViewerMotif) = {
    val unscaledX = m.layoutX / zoomLevel()
    val unscaledY = m.layoutY / zoomLevel()

    AstMap(
      "id" -> AstInteger(m.id.value),
      "x" -> AstFloat(unscaledX),
      "y" -> AstFloat(unscaledY)
    )
  }

  private def deserialiseMotif(motifData: AstMap) = {
    val id = motifData("id").asInstanceOf[AstInteger].i
    val x = motifData("x").asInstanceOf[AstFloat].f
    val y = motifData("y").asInstanceOf[AstFloat].f

    (ThematicElementID(id), x, y)
  }


  def requestRecommendation(id: NodeId): Unit = {
    EventBus.send(RecommendationRequest(id, StoryViewerEventSourceIdentity))
  }

  EventBus.RecommendationResponses filter(_.origin == StoryViewerEventSourceIdentity) foreach { r =>
    onRecommendationResponse(r.nodeId, r.recommendedNodes) }

  def onRecommendationResponse(nodeId: NodeId, recommendation: List[(Nodal, Double)]): Unit = {
    viewer.onRecommendationResponse(nodeId, recommendation)
  }
}
