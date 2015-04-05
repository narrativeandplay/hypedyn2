package org.narrativeandplay.hypedyn.events

object UIEventDispatcher {
  var selectedNodeId: Option[Long] = None

  EventBus.nodeSelectedEvents subscribe { evt => selectedNodeId = Some(evt.nodeId) }
  EventBus.nodeDeselectedEvents subscribe { evt => selectedNodeId = None }
}
