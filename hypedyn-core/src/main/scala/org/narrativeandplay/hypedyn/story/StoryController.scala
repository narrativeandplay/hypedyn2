package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeDestroyed, NodeUpdated, NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  def find(id: Long) = currentStory.nodes.find(_.id == id)

  def createNode(node: Node): Unit = {
    val newNode = new NodeImpl(node.name, node.content, firstUnusedId)
    currentStory.storyNodes += newNode
    firstUnusedId += 1

    EventBus send NodeCreated(newNode)
  }

  def destroyNode(node: Node): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove foreach (currentStory.storyNodes -= _)

    nodeToRemove foreach (EventBus send NodeDestroyed(_))
  }

  def updateNode(node: Node): Unit = {
    val nodeToUpdate = currentStory.storyNodes find (_.id == node.id)
    nodeToUpdate foreach { n => n.content = node.content; n.name = node.name }

    nodeToUpdate foreach (EventBus send NodeUpdated(_))
  }
}
