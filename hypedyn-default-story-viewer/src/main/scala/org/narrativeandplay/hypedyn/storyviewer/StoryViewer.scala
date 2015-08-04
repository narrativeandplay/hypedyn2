package org.narrativeandplay.hypedyn.storyviewer

import scala.util.Try

import scalafx.scene.control.{Control, ScrollPane}

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.events.{UiNodeDeselected, UiNodeSelected, EditNodeRequest, EventBus}
import org.narrativeandplay.hypedyn.plugins.{Saveable, Plugin}
import org.narrativeandplay.hypedyn.plugins.narrativeviewer.NarrativeViewer
import org.narrativeandplay.hypedyn.serialisation.AstElement
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}
import org.narrativeandplay.hypedyn.undo.{NodeMovedChange, UndoableStream}

class StoryViewer extends ScrollPane with Plugin with NarrativeViewer with Saveable {
  prefWidth = 800
  prefHeight = 600

  fitToHeight = true
  fitToWidth = true

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
  override def onNodeCreated(node: Nodal): Unit = viewer.addNode(node)

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
  override def onLoad(data: AstElement): Unit = ???

  /**
   * Returns the data that this Saveable would like saved
   */
  override def onSave(): AstElement = ???

  def sizeToChildren(): Unit = {
    val allBounds = (viewer.nodes map (_.bounds)).toList
    val maxX = Try((allBounds map (_.maxX)).max) getOrElse 0d
    val maxY = Try((allBounds map (_.maxY)).max) getOrElse 0d

    if (maxX > viewportBounds().getWidth) { fitToWidth = false; viewer.prefWidth = maxX } else fitToWidth = true
    if (maxY > viewportBounds().getHeight) { fitToHeight = false; viewer.prefHeight = maxY } else fitToHeight = true
  }

  def moveNode(nodeId: NodeId, position: Vector2[Double]): Unit = {
    viewer.nodes find (_.id == nodeId) foreach (_.relocate(position.x, position.y))

    sizeToChildren()
  }

  def requestNodeEdit(id: NodeId): Unit = {
    EventBus.send(EditNodeRequest(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeMove(id: NodeId, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    UndoableStream.send(new NodeMovedChange(this, id, initialPos, finalPos))
  }

  def notifyNodeSelection(id: NodeId): Unit = {
    EventBus.send(UiNodeSelected(id, StoryViewerEventSourceIdentity))
  }

  def notifyNodeDeselection(id: NodeId): Unit = {
    EventBus.send(UiNodeDeselected(id, StoryViewerEventSourceIdentity))
  }
}
