package org.narrativeandplay.hypedyn.plugins.storyviewer

import scalafx.scene.control.{Control, ScrollPane}

import com.github.benedictleejh.scala.math.vector.Vector2

import org.narrativeandplay.hypedyn.events.{EditNodeRequest, EventBus}
import org.narrativeandplay.hypedyn.plugins.storyviewer.components.ViewerNode
import org.narrativeandplay.hypedyn.plugins.{Saveable, Plugin}
import org.narrativeandplay.hypedyn.plugins.narrativeviewer.NarrativeViewer
import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{NodeId, Narrative, Nodal}
import org.narrativeandplay.hypedyn.undo.{NodeMovedChange, UndoController}

class StoryViewer extends ScrollPane with Plugin with NarrativeViewer with Saveable {
  /**
   * Returns the name of the plugin
   */
  val name: String = "Default Story Viewer"

  /**
   * Returns the version of the plugin
   */
  val version: String = "1.0.0"

  private val viewer = new StoryViewerContent(this)
  content = new Control(viewer) {}

  /**
   * Defines what to do when a story is loaded
   *
   * @param story The story that is loaded
   */
  override def onStoryLoaded(story: Narrative): Unit = {
    viewer.clear()
    story.nodes foreach onNodeCreated
  }

  /**
   * Defines what to do when a node is created
   *
   * @param node The created node
   */
  override def onNodeCreated(node: Nodal): Unit = viewer addNode node

  /**
   * Defines what to do when a node is updated
   *
   * @param node The node to be updated
   * @param updatedNode The same node with the updates already applied
   */
  override def onNodeUpdated(node: Nodal, updatedNode: Nodal): Unit = viewer updateNode (node, updatedNode)

  /**
   * Defines what to do when a node is destroyed
   *
   * @param node The node to be destroyed
   */
  override def onNodeDestroyed(node: Nodal): Unit = viewer removeNode node

  /**
   * Restore the state of this Saveable that was saved
   *
   * @param data The saved data
   */
  override def onLoad(data: AstElement): Unit = {
    val nodes = data.asInstanceOf[AstMap]("nodes").asInstanceOf[AstList].elems
    nodes foreach { n =>
      val nodeData = n.asInstanceOf[AstMap]
      val (id, x, y) = deserialise(nodeData)

      viewer.nodes find (_.id == id) foreach (_.relocate(x, y))
    }
  }

  /**
   * Returns the data that this Saveable would like saved
   */
  override def onSave(): AstElement = {
    val nodes = AstList(viewer.nodes.toList map serialise: _*)

    AstMap("nodes" -> nodes)
  }

  def requestNodeEdit(id: NodeId): Unit = {
    EventBus.send(EditNodeRequest(id, s"Plugin - $name"))
  }

  def notifyNodeMove(id: NodeId, initialPos: Vector2[Double], finalPos: Vector2[Double]): Unit = {
    UndoController.send(new NodeMovedChange(viewer, id, initialPos, finalPos))
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
