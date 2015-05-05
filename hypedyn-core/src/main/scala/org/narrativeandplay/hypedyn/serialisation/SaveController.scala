package org.narrativeandplay.hypedyn.serialisation

import org.narrativeandplay.hypedyn.plugins.SaveablesController
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
    SaveablesController.onLoad(actualData("plugins").asInstanceOf[SaveHash])
  }
}
