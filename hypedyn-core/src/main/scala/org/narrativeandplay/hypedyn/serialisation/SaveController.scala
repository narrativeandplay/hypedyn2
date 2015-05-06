package org.narrativeandplay.hypedyn.serialisation

import org.narrativeandplay.hypedyn.events.{StoryLoadedEvent, EventBus}
import org.narrativeandplay.hypedyn.plugins.{PluginsController, SaveablesController}
import org.narrativeandplay.hypedyn.story.StoryController

object SaveController {
  def getSaveData = {
    val storyData = StoryController.save
    val pluginData = SaveablesController.onSave

    SaveHash("story" -> storyData, "plugins" -> pluginData)
  }

  def loadSaveData(data: SaveElement): Unit = {
    val actualData = data.asInstanceOf[SaveHash]

    StoryController.load(actualData("story").asInstanceOf[SaveHash])
    PluginsController.Plugins.values foreach (_.onStoryLoaded(StoryController.story)) //has to be called directly because RxScala is async. stupid async

    SaveablesController.onLoad(actualData("plugins").asInstanceOf[SaveHash])
  }
}
