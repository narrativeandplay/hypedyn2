package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.serialisation.SaveHash
import org.narrativeandplay.hypedyn.story.{Story, Node}

sealed trait Event

sealed trait Request extends Event
case object CreateNodeRequest extends Request
case object EditNodeRequest extends Request
case object DeleteNodeRequest extends Request

sealed trait Action extends Event
sealed case class CreateNode(node: Node) extends Action
sealed case class EditNode(node: Node) extends Action
sealed case class DeleteNode(node: Node) extends Action

sealed trait Completion extends Event
sealed case class NodeCreated(node: Node) extends Completion
sealed case class NodeEdited(node: Node) extends Completion
sealed case class NodeDeleted(node: Node) extends Completion


case object SaveEvent extends Event
sealed case class LoadEvent(data: SaveHash) extends Event

sealed case class StoryLoadedEvent(story: Story) extends Event