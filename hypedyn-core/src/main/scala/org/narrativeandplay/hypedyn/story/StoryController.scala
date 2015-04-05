package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  EventBus.createNodeEvents subscribe { evt =>
    addNode(evt.node)
  }

  def addNode(node: Node): Unit = {
    val newNode = new NodeImpl(node.name, node.content, firstUnusedId)
    currentStory.storyNodes += newNode
    firstUnusedId += 1

    EventBus send NodeCreated(newNode)
  }
  def deleteNode(node: Node): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove map (currentStory.storyNodes -= _)
  }
  def updateNode(node: Node): Unit = {
    currentStory.storyNodes find (_.id == node.id) foreach { n => n.content = node.content; n.name = node.name }
  }
}
