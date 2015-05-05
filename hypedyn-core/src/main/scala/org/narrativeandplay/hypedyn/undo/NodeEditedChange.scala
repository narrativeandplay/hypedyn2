package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.story.{NodeLike, StoryController}

class NodeEditedChange(editedNode: NodeLike, uneditedNode: NodeLike) extends Change {
  override def undo(): Unit = StoryController update (editedNode, uneditedNode, undoable = false)

  override def redo(): Unit = StoryController update (uneditedNode, editedNode, undoable = false)
}
