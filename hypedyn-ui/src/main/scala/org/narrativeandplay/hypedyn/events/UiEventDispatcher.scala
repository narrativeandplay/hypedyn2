package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.dialogs.NodeEditor

object UiEventDispatcher {
  var selectedNodeId: Option[Long] = None

  EventBus.newNodeEvents subscribe { evt =>
    val newNode = new NodeEditor("New Node").showAndWait()

    newNode foreach (EventBus send CreateNode(_))
  }

  EventBus.editNodeEvents subscribe { evt =>
    val editedNode = new NodeEditor("Edit Node", evt.node).showAndWait()

    editedNode foreach (EventBus send UpdateNode(evt.node, _))
  }

  EventBus.nodeSelectedEvents subscribe { evt => selectedNodeId = Some(evt.nodeId) }
  EventBus.nodeDeselectedEvents subscribe { evt => selectedNodeId = None }
}
