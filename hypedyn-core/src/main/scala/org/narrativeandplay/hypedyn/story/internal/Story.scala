package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.serialisation.{SaveList, SaveString, SaveHash}
import org.narrativeandplay.hypedyn.story.{NodeLike, StoryLike}
import org.narrativeandplay.hypedyn.story.internal

class Story(val title: String = "Untitled",
            val author: String = "",
            val description: String = "",
            val nodes: Set[Node] = Set.empty) extends StoryLike {
  type NodeType = Node

  def serialise = SaveHash("title" -> SaveString(title),
                           "author" -> SaveString(author),
                           "description" -> SaveString(description),
                           "nodes" -> SaveList((nodes map (_.serialise)).toSeq: _*))

  def retitle(newTitle: String) = new Story(newTitle, author, description, nodes)
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, nodes)
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, nodes)

  def addNode(node: Node) = new Story(title, author, description, nodes + node)
  def removeNode(node: Node) = new Story(title, author, description, nodes - node)
  def updateNode(oldNode: Node, newNode: Node) = new Story(title,
                                                           author,
                                                           description,
                                                           nodes - oldNode + newNode)
}

object Story {
  def deserialise(storyHash: SaveHash) = {
    val title = storyHash("title").asInstanceOf[SaveString].s
    val author = storyHash("author").asInstanceOf[SaveString].s
    val description = storyHash("description").asInstanceOf[SaveString].s
    val nodes = Set(storyHash("nodes").asInstanceOf[SaveList].elems map { n => Node deserialise n.asInstanceOf[SaveHash] }: _*)

    new Story(title, author, description, nodes)
  }
}
