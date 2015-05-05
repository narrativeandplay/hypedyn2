package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.{Node, Story}

class NodeDestroyedChange(destroyedNode: Node) extends Change {
  override def undo(): Unit = StoryController create (destroyedNode, undoable = false)

  override def redo(): Unit = StoryController destroy (destroyedNode, undoable = false)
}
