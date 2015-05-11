package org.narrativeandplay.hypedyn.plugins

import org.narrativeandplay.hypedyn.story.Narrative

trait Plugin {
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
