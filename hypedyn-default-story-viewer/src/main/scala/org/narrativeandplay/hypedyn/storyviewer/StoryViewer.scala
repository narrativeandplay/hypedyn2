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
import org.narrativeandplay.hypedyn.plugins.{Plugin, Saveable}
import org.narrativeandplay.hypedyn.plugins.narrativeviewer.NarrativeViewer
import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.storyviewer.components.ViewerNode
import org.narrativeandplay.hypedyn.storyviewer.utils.DoubleUtils
import org.narrativeandplay.hypedyn.undo.{NodeMovedChange, UndoableStream}

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
  val zoomLevel = DoubleProperty(1.0)

  val StoryViewerEventSourceIdentity = s"Plugin - $name"
  val viewer = new StoryViewerContent(this)

  content = new Control(viewer) {}

  private[storyviewer] def zoomValueClamp(v: Double) = DoubleUtils.clamp(minZoom, maxZoom, v)

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
    zoomLevel() = 1.0
    nodeLocations.clear()

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

    nodeLocations get createdNode.id foreach {position : Vector2[Double] =>
      moveNode(createdNode.id, Vector2(position.x*zoomLevel(), position.y*zoomLevel()))}
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
   * Restore the state of this Saveable that was saved
   *
   * @param data The saved data
   */
  override def onLoad(data: AstElement): Unit = {
    val properData = data.asInstanceOf[AstMap]
    val nodes = properData("nodes").asInstanceOf[AstList].elems
    nodes foreach { n =>
      val nodeData = n.asInstanceOf[AstMap]
      val (id, x, y) = deserialise(nodeData)

      moveNode(id, Vector2(x, y))
    }

    zoomLevel() = properData get "zoomLevel" map (_.asInstanceOf[AstFloat].f) getOrElse 1.0

    sizeToChildren()
  }

  /**
   * Returns the data that this Saveable would like saved
   */
  override def onSave(): AstElement = AstMap("zoomLevel" -> AstFloat(zoomLevel()),
                                             "nodes" -> AstList(viewer.nodes.toList map serialise: _*))

  /**
   * Resizes the content control to ensure all nodes are shown
   */
  def sizeToChildren(): Unit = {
    val allBounds = (viewer.nodes map (_.bounds)).toList
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
    updateNodeLocations(nodeId, position)

    viewer.nodes find (_.id == nodeId) foreach (_.relocate(position.x, position.y))

    sizeToChildren()
  }

  def requestNodeEdit(id: NodeId): Unit = {
    EventBus.send(EditNodeRequest(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeMove(id: NodeId, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    updateNodeLocations(id, finalPos)

    UndoableStream.send(new NodeMovedChange(this, id, initialPos, finalPos))
  }

  def updateNodeLocations(id: NodeId, pos: Vector2[Double]): Unit = {
    nodeLocations += id -> Vector2(pos.x / zoomLevel(), pos.y / zoomLevel())
  }

  def notifyNodeSelection(id: NodeId): Unit = {
    EventBus.send(UiNodeSelected(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeDeselection(id: NodeId): Unit = {
    EventBus.send(UiNodeDeselected(id, StoryViewerEventSourceIdentity))
  }

  EventBus.ZoomRequests foreach { _ =>
    EventBus.send(ZoomResponse(StoryViewerEventSourceIdentity))
  }

  EventBus.ResetZoomRequests foreach { _ =>
    EventBus.send(ResetZoomResponse(StoryViewerEventSourceIdentity))
  }

  EventBus.ZoomStoryViewEvents foreach { evt =>
    zoomLevel() = zoomValueClamp(zoomLevel() + evt.deltaZoom)
    EventBus.send(StoryViewZoomed(StoryViewerEventSourceIdentity))
  }

  EventBus.ResetStoryViewZoomEvents foreach { _ =>
    zoomLevel() = 1.0
    EventBus.send(StoryViewZoomReset(StoryViewerEventSourceIdentity))
  }

  private def serialise(n: ViewerNode) = {
    val unscaledX = n.layoutX / zoomLevel()
    val unscaledY = n.layoutY / zoomLevel()

    AstMap(
      "id" -> AstInteger(n.id.value),
      "x" -> AstFloat(unscaledX),
      "y" -> AstFloat(unscaledY)
    )
  }

  private def deserialise(nodeData: AstMap) = {
    val id = nodeData("id").asInstanceOf[AstInteger].i
    val x = nodeData("x").asInstanceOf[AstFloat].f
    val y = nodeData("y").asInstanceOf[AstFloat].f

    (NodeId(id), x, y)
  }
}
