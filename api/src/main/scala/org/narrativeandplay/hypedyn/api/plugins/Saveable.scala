package org.narrativeandplay.hypedyn.api.plugins

import org.narrativeandplay.hypedyn.api.events.{SaveData, EventBus}
import org.narrativeandplay.hypedyn.api.serialisation.AstElement

trait Saveable {
  /**
   * Ensure that a Saveable is also a Plugin
   */
  this: Plugin =>

  EventBus.SaveToFileEvents foreach { _ => EventBus.send(SaveData(name, onSave, s"Plugin - $name")) }
  EventBus.DataLoadedEvents foreach { evt => onLoad(evt.data(name)) }

  /**
   * Returns the data that this Saveable would like saved
   */
  def onSave(): AstElement

  /**
   * Restore the state of this Saveable that was saved
   *
   * @param data The saved data
   */
  def onLoad(data: AstElement): Unit
}
