package org.narrativeandplay.hypedyn.undo

import org.narrativeandplay.hypedyn.events.UndoEventDispatcher
import org.narrativeandplay.hypedyn.story.internal.Story

final case class StoryPropertiesChange(oldMetadata: Story.Metadata,
                                       newMetadata: Story.Metadata) extends Undoable {
  /**
   * Defines the change to produce when an undo action happens
   */
  override def undo(): Undoable = StoryPropertiesChange(newMetadata, oldMetadata)

  /**
   * Defines how to reverse an undo action
   */
  override def redo(): Unit = UndoEventDispatcher.updateStoryProperties(newMetadata)
}
