package org.narrativeandplay.hypedyn.plugins

import org.narrativeandplay.hypedyn.events.{LoadEvent, SaveEvent, EventBus}
import org.narrativeandplay.hypedyn.serialisation.SaveElement

trait Saveable {
  /**
   * Ensures that anything that extends Saveable is also a Plugin.
   */
  this: Plugin =>

  EventBus.loadEvents.subscribe((evt: LoadEvent) => {
    onLoad(evt.data(name))
  })

  /**
   * Returns the formatted data that a plugin wishes to save
   */
  def onSave: SaveElement

  /**
   * Transforms formatted data into a plugin's internal data structures
   *
   * @param data A formatted data object
   */
  def onLoad(data: SaveElement): Unit
}
