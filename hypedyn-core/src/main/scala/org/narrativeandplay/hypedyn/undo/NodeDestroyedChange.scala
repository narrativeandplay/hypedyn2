package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

class NodeDestroyedChange(destroyedNode: NodeImpl) extends Change[StoryImpl] {
  override def undo(): Unit = StoryController createNode (destroyedNode, undoable = false)

  override def redo(): Unit = StoryController destroyNode (destroyedNode, undoable = false)
}
