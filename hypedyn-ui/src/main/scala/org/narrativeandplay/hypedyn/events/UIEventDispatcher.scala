package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.dialogs.NodeEditor

object UIEventDispatcher {
  var selectedNodeId: Option[Long] = None

  EventBus.newNodeEvents subscribe { evt =>
    val newNode = new NodeEditor("New Node").showAndWait()

    newNode foreach { node => EventBus send CreateNode(node) }
  }

  EventBus.editNodeEvents subscribe { evt =>
    val editedNode = new NodeEditor("Edit Node", evt.node).showAndWait()

    editedNode foreach (EventBus send UpdateNode(_))
  }

  EventBus.nodeSelectedEvents subscribe { evt => selectedNodeId = Some(evt.nodeId) }
  EventBus.nodeDeselectedEvents subscribe { evt => selectedNodeId = None }
}
