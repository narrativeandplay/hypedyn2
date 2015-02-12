package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.plugins.serialisation.SaveHash
import org.narrativeandplay.hypedyn.story.Node

sealed trait Event

sealed case class EditNodeEvent(nodeId: Long) extends Event
sealed case class DeleteNodeEvent(nodeId: Long) extends Event
case object NewNodeEvent extends Event

sealed case class NodeEditedEvent(node: Node) extends Event
sealed case class NodeCreatedEvent(node: Node) extends Event
sealed case class NodeDeletedEvent(node: Node) extends Event

case object SaveEvent extends Event
sealed case class LoadEvent(data: SaveHash) extends Event