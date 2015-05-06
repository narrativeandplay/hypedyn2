package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.{Saveable, Plugin}
import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{StoryLike, NodeLike}

import scalafx.scene.control.{Control, ScrollPane}

class StoryViewer extends ScrollPane with Plugin with StoryViewerLike with Saveable {
  prefWidth = 800
  prefHeight = 600

  /**
   * Returns the name of the plugin
   */
  val name: String = "Default Story Viewer"

  /**
   * Returns the version of the plugin (as per Semantic Versioning 2.0.0 - see http://semver.org/spec/v2.0.0.html)
   */
  val version: String = "1.0.0"

  val viewer = new StoryViewerContent
  content = new Control(viewer) {}

  override def onNodeCreated(node: NodeLike): Unit = viewer addNode node

  override def onNodeUpdated(node: NodeLike): Unit = viewer updateNode node

  override def onNodeDestroyed(node: NodeLike): Unit = viewer removeNode node

  override def onStoryLoaded(story: StoryLike): Unit = {
    viewer.clear()
    story.nodes foreach onNodeCreated
  }

  /**
   * Returns the formatted data that a plugin wishes to save
   */
  override def onSave: SaveElement = {
    def serialise(n: ViewerNode) = SaveHash("id" -> SaveInt(n.id),
                                            "x" -> SaveFloat(n.layoutX),
                                            "y" -> SaveFloat(n.layoutY))

    val nodes = SaveList(viewer.nodes.toList map serialise: _*)

    SaveHash("nodes" -> nodes)
  }

  /**
   * Transforms formatted data into a plugin's internal data structures
   *
   * @param data A formatted data object
   */
  override def onLoad(data: SaveElement): Unit = {
    def deserialise(nodeData: SaveHash) = {
      val id = nodeData("id").asInstanceOf[SaveInt].i
      val x = nodeData("x").asInstanceOf[SaveFloat].f
      val y = nodeData("y").asInstanceOf[SaveFloat].f

      (id, x, y)
    }
    val nodes = data.asInstanceOf[SaveHash]("nodes").asInstanceOf[SaveList].elems
    nodes foreach { n =>
      val nodeData = n.asInstanceOf[SaveHash]
      val (id, x, y) = deserialise(nodeData)

      viewer.nodes find (_.id == id) foreach (_.relocate(x, y))
    }
  }
}
