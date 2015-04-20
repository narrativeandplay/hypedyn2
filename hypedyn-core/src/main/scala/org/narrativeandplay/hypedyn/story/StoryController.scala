package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeDestroyed, NodeUpdated, NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  def find(id: Long) = currentStory.nodes.find(_.id == id)

  def createNode(node: Node, undoable: Boolean = true): Unit = {
    currentStory.storyNodes += newNode
    firstUnusedId += 1

    EventBus send NodeCreated(newNode)
  }

  def destroyNode(node: Node, undoable: Boolean = true): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove foreach (currentStory.storyNodes -= _)

    nodeToRemove foreach (EventBus send NodeDestroyed(_))
  }

  def updateNode(uneditedNode: Node, editedNode: Node, undoable: Boolean = true): Unit = {
    val nodeToUpdate = currentStory.storyNodes find (_.id == uneditedNode.id)
    nodeToUpdate foreach { n => n.content = editedNode.content; n.name = editedNode.name }

    nodeToUpdate foreach (EventBus send NodeUpdated(_))
  }
}
