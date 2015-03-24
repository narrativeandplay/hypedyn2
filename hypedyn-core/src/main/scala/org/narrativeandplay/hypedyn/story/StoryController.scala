package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.events.{NodeCreated, EventBus}
import org.narrativeandplay.hypedyn.story.internal.{NodeImpl, StoryImpl}

object StoryController {
  var currentStory = new StoryImpl()
  var firstUnusedId = 0.toLong

  EventBus.createNodeEvents subscribe { evt =>
    addNode(evt.node)
  }

  def init(): Unit = {
    currentStory = new StoryImpl()
    firstUnusedId = 0
  }

  def addNode(node: Node): Unit = {
    println("hello")
    println(node)
    println("hello1")
    val newNode = new Node {
      override def name: String = node.name

      override def content: String = node.content

      override def id: Long = firstUnusedId
    }
    //currentStory.storyNodes += newNode
    println("hello2")
    firstUnusedId += 1
    println("hello3")

    EventBus send NodeCreated(newNode)
    println("goodbye")
  }
  def deleteNode(node: Node): Unit = {
    val nodeToRemove = currentStory.storyNodes find (_.id == node.id)
    nodeToRemove map (currentStory.storyNodes -= _)
  }
  def updateNode(node: Node): Unit = {
    currentStory.storyNodes find (_.id == node.id) map { n => n.content = node.content; n.name = node.name }
  }
}
