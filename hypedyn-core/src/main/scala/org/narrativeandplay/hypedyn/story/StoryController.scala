package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  def addNode(node: Node): Unit = {
    currentStory.storyNodes += new NodeImpl(node.name, node.content, firstUnusedId)
    firstUnusedId += 1
  }
  def deleteNode(node: Node): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove map (currentStory.storyNodes -= _)
  }
  def updateNode(node: Node): Unit = {
    currentStory.storyNodes find (_.id == node.id) map { n => n.content = node.content; n.name = node.name }
  }
}
