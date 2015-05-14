package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.internal.{Node, Story}

object StoryController {
  private var currentStory = new Story()
  private var firstUnusedId = NodeId(0)

  def story = currentStory

  def newStory(title: String, author: String, description: String): Unit = {
    currentStory = new Story(title, author, description)
  }

  def load(story: Story): Unit = {
    currentStory = story
    firstUnusedId = (story.nodes map (_.id)).max.inc
  }

  def find(nodeId: NodeId) = currentStory.nodes find (_.id == nodeId)

  def create(node: Nodal): Node = {
    val newNode = Node(if (node.id.isValid) node.id else firstUnusedId, node.name, node.content, node.isStartNode)
    currentStory = currentStory addNode newNode
    firstUnusedId = List(firstUnusedId, newNode.id.inc).max

    newNode
  }

  def update(node: Nodal, editedNode: Nodal): Option[(Node, Node)] = {
    val toUpdate = find(node.id)
    val updated = new Node(editedNode.id, editedNode.name, editedNode.content, editedNode.isStartNode)

    toUpdate foreach { n => currentStory = currentStory updateNode (n, updated) }

    toUpdate map ((_, updated))
  }

  def destroy(node: Nodal): Option[Node] = {
    val toDestroy = find(node.id)

    toDestroy foreach { n => currentStory = currentStory removeNode n }

    toDestroy
  }

}
