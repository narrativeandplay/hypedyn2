package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.clipboard.ClipboardController
import org.narrativeandplay.hypedyn.serialisation.IoController
import org.narrativeandplay.hypedyn.story.StoryController

object EventDispatcher {
  EventBus.newNodeRequests subscribe { evt => EventBus send NewNode }
  EventBus.editNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send EditNode(_)) }
  EventBus.deleteNodeRequests subscribe { evt => StoryController find evt.nodeId foreach (EventBus send DestroyNode(_)) }

  EventBus.createNodeEvents subscribe { evt => StoryController create evt.node }
  EventBus.updateNodeEvents subscribe { evt => StoryController update (evt.uneditedNode, evt.editedNode) }
  EventBus.destroyNodeEvents subscribe { evt => StoryController destroy evt.node }

  EventBus.cutNodeRequests subscribe { evt => StoryController find evt.nodeId foreach ClipboardController.cut }
  EventBus.copyNodeRequests subscribe { evt => StoryController find evt.nodeId foreach ClipboardController.copy }
  EventBus.pasteNodeRequests subscribe { evt => ClipboardController.paste() }

  EventBus.saveEvents subscribe { evt => IoController.save(evt.saveFile) }
  EventBus.loadEvents subscribe { evt => IoController.load(evt.saveFile) }
}
