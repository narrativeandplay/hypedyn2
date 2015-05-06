package org.narrativeandplay.hypedyn.plugins

import org.narrativeandplay.hypedyn.events.EventBus
import org.narrativeandplay.hypedyn.story.StoryLike

trait Plugin {
  /**
   * Returns the name of the plugin
   */
  def name: String

  /**
   * Returns the version of the plugin (as per Semantic Versioning 2.0.0 - see http://semver.org/spec/v2.0.0.html)
   */
  def version: String //TODO: change this to a SemVer Version structure

  EventBus.storyLoadedEvents subscribe { evt => onStoryLoaded(evt.story) }
  def onStoryLoaded(story: StoryLike): Unit = {}
}
