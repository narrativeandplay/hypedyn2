package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.story.StoryController

object EventDispatcher {
  EventBus.newNodeRequests subscribe { evt => EventBus send NewNode }
//  EventBus.editNodeRequests subscribe { evt => EventBus send EditNode() }

  EventBus.createNodeEvents subscribe { evt => StoryController addNode evt.node }
  EventBus.updateNodeEvents subscribe { evt => StoryController updateNode evt.node }
  EventBus.destroyNodeEvents subscribe { evt => StoryController deleteNode evt.node }
}
