package org.narrativeandplay.hypedyn.events

import java.io.File

import org.narrativeandplay.hypedyn.serialisation.SaveHash
import org.narrativeandplay.hypedyn.story.{StoryLike, NodeLike}

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

sealed case class EditNode(node: NodeLike) extends UIAction

sealed case class DeleteNode(node: NodeLike) extends UIAction

sealed trait Action extends Event
sealed case class CreateNode(node: NodeLike) extends Action
sealed case class UpdateNode(uneditedNode: NodeLike, editedNode: NodeLike) extends Action
sealed case class DestroyNode(node: NodeLike) extends Action


sealed trait Completion extends Event
sealed case class NodeCreated(node: NodeLike) extends Completion
sealed case class NodeUpdated(node: NodeLike) extends Completion
sealed case class NodeDestroyed(node: NodeLike) extends Completion
sealed case class NodeSelected(nodeId: Long) extends Completion
sealed case class NodeDeselected(nodeId: Long) extends Completion
sealed case class StoryLoadedEvent(story: StoryLike) extends Completion


case class SaveEvent(saveFile: File) extends Event
case class LoadEvent(saveFile: File) extends Event
