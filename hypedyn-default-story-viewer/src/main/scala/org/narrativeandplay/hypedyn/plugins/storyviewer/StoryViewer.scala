package org.narrativeandplay.hypedyn.plugins.storyviewer

import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.NodeLike

import scalafx.scene.control.ScrollPane

class StoryViewer extends ScrollPane with Plugin with StoryViewerLike {
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
  content = viewer

  override def onNodeCreated(node: NodeLike): Unit = viewer addNode node

  override def onNodeUpdated(node: NodeLike): Unit = viewer updateNode node

  override def onNodeDestroyed(node: NodeLike): Unit = viewer removeNode node
}
