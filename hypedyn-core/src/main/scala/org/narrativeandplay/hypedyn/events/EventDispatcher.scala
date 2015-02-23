package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.story.StoryController

object EventDispatcher {
  EventBus.createNodeEvents subscribe { evt => StoryController.addNode(evt.node) }
  EventBus.updateNodeEvents subscribe { evt => StoryController.updateNode(evt.node) }
  EventBus.destroyNodeEvents subscribe { evt => StoryController.deleteNode(evt.node) }
}
