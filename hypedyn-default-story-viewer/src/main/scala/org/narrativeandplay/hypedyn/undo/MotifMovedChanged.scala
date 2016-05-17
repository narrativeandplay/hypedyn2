package org.narrativeandplay.hypedyn.undo

import com.github.benedictleejh.scala.math.vector.Vector2
import org.narrativeandplay.hypedyn.story.themes.ThematicElementID
import org.narrativeandplay.hypedyn.storyviewer.StoryViewer

/**
  * Change representing a theme having been moved
  *
  * @param eventHandler The event dispatcher allowed to send events
  * @param motifId The ID of the motif moved
  * @param initialPos The initial position of the motif
  * @param finalPos The final position of the motif
  */
case class MotifMovedChange(eventHandler: StoryViewer,
                            motifId: ThematicElementID,
                            initialPos: Vector2[Double],
                            finalPos: Vector2[Double]) extends Undoable {
  override def undo(): MotifMovedChange = MotifMovedChange(eventHandler, motifId, finalPos, initialPos)

  override def redo(): Unit = {
    eventHandler.moveMotif(motifId, finalPos)
    eventHandler.notifyMotifMove(motifId, initialPos, finalPos)
  }

  override def merge(other: Undoable): Option[Undoable] = other match {
    case c: MotifMovedChange =>
      if (motifId == c.motifId) Some(new MotifMovedChange(eventHandler, motifId, initialPos, c.finalPos)) else None
    case _ => None
  }
}