package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeDestroyed, NodeUpdated, NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.serialisation.SaveHash
import org.narrativeandplay.hypedyn.story.internal.{Node, Story}
import org.narrativeandplay.hypedyn.undo.{NodeDestroyedChange, NodeEditedChange, NodeCreatedChange, UndoController}

object StoryController {
  private var currentStory = new Story()
  private var firstUnusedId = 0L

  def story = currentStory

  def find(id: Long) = currentStory.nodes find (_.id == id)

  def create(node: NodeLike, undoable: Boolean = true): Unit = {
    val newNode = new Node(node.name, node.content, if (node.id < 0) firstUnusedId else node.id)
    currentStory = currentStory addNode newNode
    firstUnusedId = math.max(firstUnusedId, newNode.id + 1)

    EventBus send NodeCreated(newNode)

    if (undoable) {
      UndoController send new NodeCreatedChange(newNode)
    }
  }

  def destroy(node: NodeLike, undoable: Boolean = true): Unit = {
    val nodeToRemove = currentStory.nodes find (_.id == node.id)
    nodeToRemove foreach { n => currentStory = currentStory removeNode n }

    nodeToRemove foreach (EventBus send NodeDestroyed(_))

    if (undoable) {
      nodeToRemove foreach (UndoController send new NodeDestroyedChange(_))
    }
  }

  def update(uneditedNode: NodeLike, editedNode: NodeLike, undoable: Boolean = true): Unit = {
    val nodeToUpdate = currentStory.nodes find (_.id == uneditedNode.id)
    val updatedNode = new Node(editedNode.name, editedNode.content, editedNode.id)
    nodeToUpdate foreach { n => currentStory = currentStory updateNode (n, updatedNode) }

    EventBus.send(NodeUpdated(updatedNode))

    if (undoable) {
      UndoController send new NodeEditedChange(editedNode, uneditedNode)
    }
  }

  def save = currentStory.serialise

  def load(story: SaveHash): Unit = {
    currentStory = Story deserialise story
    firstUnusedId = (currentStory.nodes map (_.id)).max + 1
  }
}
