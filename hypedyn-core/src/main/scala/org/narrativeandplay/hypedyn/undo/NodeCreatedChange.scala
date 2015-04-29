package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.Node

class NodeCreatedChange(createdNode: Node) extends Change {
  override def undo(): Unit = StoryController destroyNode (createdNode, undoable = false)

  override def redo(): Unit = StoryController createNode (createdNode, undoable = false)
}
