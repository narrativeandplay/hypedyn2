package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.serialisation.SaveHash
import org.narrativeandplay.hypedyn.story.{Story, Node}

sealed trait Event

sealed trait Request extends Event

case object NewNodeRequest extends Request

sealed case class EditNodeRequest(nodeId: Long) extends Request

sealed case class DeleteNodeRequest(nodeId: Long) extends Request

sealed case class CutNodeRequest(nodeId: Long) extends Request

sealed case class CopyNodeRequest(nodeId: Long) extends Request

case object PasteNodeRequest extends Request

sealed trait UIAction extends Event

case object NewNode extends UIAction

sealed case class EditNode(node: Node) extends UIAction

sealed case class DeleteNode(node: Node) extends UIAction

sealed trait Action extends Event

sealed case class CreateNode(node: Node) extends Action

sealed case class UpdateNode(uneditedNode: Node, editedNode: Node) extends Action

sealed case class DestroyNode(node: Node) extends Action

sealed trait Completion extends Event

sealed case class NodeCreated(node: Node) extends Completion

sealed case class NodeUpdated(node: Node) extends Completion

sealed case class NodeDestroyed(node: Node) extends Completion

sealed case class NodeSelected(nodeId: Long) extends Completion

sealed case class NodeDeselected(nodeId: Long) extends Completion


case object SaveEvent extends Event

sealed case class LoadEvent(data: SaveHash) extends Event

sealed case class StoryLoadedEvent(story: Story) extends Event