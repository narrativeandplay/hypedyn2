package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.{NodeLike, StoryController}

class NodeEditedChange(edited: NodeLike, unedited: NodeLike) extends Change {
  override def undo(): Unit = StoryController updateNode (edited, unedited, undoable = false)

  override def redo(): Unit = StoryController updateNode (unedited, edited, undoable = false)
}
