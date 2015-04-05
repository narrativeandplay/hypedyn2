package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.dialogs.NodeEditor

object UIEventDispatcher {
  var selectedNodeId: Option[Long] = None

  EventBus.newNodeEvents subscribe { evt =>
    val newNode = new NodeEditor("New Node").showAndWait()

    newNode foreach { node => EventBus send CreateNode(node) }
  }

  EventBus.nodeSelectedEvents subscribe { evt => selectedNodeId = Some(evt.nodeId) }
  EventBus.nodeDeselectedEvents subscribe { evt => selectedNodeId = None }
}
