package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

class NodeCreatedChange(createdNode: NodeImpl) extends Change[StoryImpl] {
  override def undo(): Unit = StoryController destroyNode (createdNode, undoable = false)

  override def redo(): Unit = StoryController createNode (createdNode, undoable = false)
}
