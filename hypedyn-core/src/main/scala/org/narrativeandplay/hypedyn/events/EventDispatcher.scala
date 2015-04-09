package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.story.StoryController

object EventDispatcher {
  EventBus.newNodeRequests subscribe { evt => EventBus send NewNode }
  EventBus.editNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send EditNode(_)) }
  EventBus.deleteNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send DestroyNode(_)) }

  EventBus.createNodeEvents subscribe { evt => StoryController createNode evt.node }
  EventBus.updateNodeEvents subscribe { evt => StoryController updateNode evt.node }
  EventBus.destroyNodeEvents subscribe { evt => StoryController destroyNode evt.node }
}
