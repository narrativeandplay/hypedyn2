package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.{Node, StoryController}
import org.narrativeandplay.hypedyn.story.internal.NodeImpl

class NodeEditedChange(edited: Node, unedited: Node) extends Change {
  override def undo(): Unit = StoryController updateNode (edited, unedited, undoable = false)

  override def redo(): Unit = StoryController updateNode (unedited, edited, undoable = false)
}
