package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeDestroyed, NodeUpdated, NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}
import org.narrativeandplay.hypedyn.undo.{NodeDestroyedChange, NodeEditedChange, NodeCreatedChange, UndoController}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  def find(id: Long) = currentStory.nodes.find(_.id == id)

  def createNode(node: Node, undoable: Boolean = true): Unit = {
    val newNode = new NodeImpl(node.name, node.content, if (node.id < 0) firstUnusedId else node.id)
    currentStory.storyNodes += newNode
    firstUnusedId = math.max(firstUnusedId, node.id + 1)

    EventBus send NodeCreated(newNode)

    if (undoable) {
      UndoController send new NodeCreatedChange(newNode)
    }
  }

  def destroyNode(node: Node, undoable: Boolean = true): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove foreach (currentStory.storyNodes -= _)

    nodeToRemove foreach (EventBus send NodeDestroyed(_))

    if (undoable) {
      nodeToRemove foreach (UndoController send new NodeDestroyedChange(_))
    }
  }

  def updateNode(uneditedNode: Node, editedNode: Node, undoable: Boolean = true): Unit = {
    val nodeToUpdate = currentStory.storyNodes find (_.id == uneditedNode.id)
    nodeToUpdate foreach { n => n.content = editedNode.content; n.name = editedNode.name }

    nodeToUpdate foreach (EventBus send NodeUpdated(_))

    if (undoable) {
      UndoController send new NodeEditedChange(editedNode, uneditedNode)
    }
  }
}
