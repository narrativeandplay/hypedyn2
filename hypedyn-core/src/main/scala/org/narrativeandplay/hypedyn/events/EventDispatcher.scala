package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.clipboard.ClipboardController
import org.narrativeandplay.hypedyn.story.StoryController

object EventDispatcher {
  EventBus.newNodeRequests subscribe { evt => EventBus send NewNode }
  EventBus.editNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send EditNode(_)) }
  EventBus.deleteNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send DestroyNode(_)) }

  EventBus.createNodeEvents subscribe { evt => StoryController createNode evt.node }
  EventBus.updateNodeEvents subscribe { evt => StoryController updateNode (evt.uneditedNode, evt.editedNode) }
  EventBus.destroyNodeEvents subscribe { evt => StoryController destroyNode evt.node }

  EventBus.cutNodeRequests subscribe { evt => StoryController find evt.nodeId foreach ClipboardController.cut }
  EventBus.copyNodeRequests subscribe { evt => StoryController find evt.nodeId foreach ClipboardController.copy }
  EventBus.pasteNodeRequests subscribe { evt => ClipboardController.paste() }
}
