package org.narrativeandplay.hypedyn.api.plugins

import org.narrativeandplay.hypedyn.api.events.EventBus
import org.narrativeandplay.hypedyn.api.story.Narrative

/**
 * An interface for a generic HypeDyn plugin
 */
trait Plugin {
  EventBus.StoryLoadedEvents foreach { evt => onStoryLoaded(evt.story) }

  /**
   * Returns the name of the plugin
   */
  def name: String

  /**
   * Returns the version of the plugin
   */
  def version: String

  /**
   * Defines what to do when a story is loaded
   *
   * @param story The story that is loaded
   */
  def onStoryLoaded(story: Narrative): Unit
}
