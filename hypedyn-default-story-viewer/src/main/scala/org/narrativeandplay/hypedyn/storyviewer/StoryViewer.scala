package org.narrativeandplay.hypedyn.storyviewer

import scala.collection.mutable
import scala.util.Try

import scalafx.beans.property.DoubleProperty
import scalafx.scene.control.{Control, ScrollPane}

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.events.{UiNodeDeselected, UiNodeSelected, EditNodeRequest, EventBus}
import org.narrativeandplay.hypedyn.plugins.{Saveable, Plugin}
import org.narrativeandplay.hypedyn.plugins.narrativeviewer.NarrativeViewer
import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.storyviewer.components.ViewerNode
import org.narrativeandplay.hypedyn.undo.{NodeMovedChange, UndoableStream}

/**
 * StoryViewer implementation class
 */
class StoryViewer extends ScrollPane with Plugin with NarrativeViewer with Saveable {
  prefWidth = 800
  prefHeight = 600

  fitToHeight = true
  fitToWidth = true

  private val minZoom = 0.5
  private val maxZoom = 1.0

  val nodeLocations = mutable.Map.empty[NodeId, Vector2[Double]]
  val zoomLevel = DoubleProperty(1.0)
  zoomLevel onChange { (_, _, value) =>
    // clamps the zoom level to between the min and max zoom levels
    zoomLevel() = minZoom max value.doubleValue() min maxZoom
  }

  val StoryViewerEventSourceIdentity = s"Plugin - $name"
  val viewer = new StoryViewerContent(this)

  content = new Control(viewer) {}

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

  private def serialise(n: ViewerNode) = AstMap("id" -> AstInteger(n.id.value),
                                                "x" -> AstFloat(n.layoutX),
                                                "y" -> AstFloat(n.layoutY))
  private def deserialise(nodeData: AstMap) = {
    val id = nodeData("id").asInstanceOf[AstInteger].i
    val x = nodeData("x").asInstanceOf[AstFloat].f
    val y = nodeData("y").asInstanceOf[AstFloat].f

    (NodeId(id), x, y)
  }
}
