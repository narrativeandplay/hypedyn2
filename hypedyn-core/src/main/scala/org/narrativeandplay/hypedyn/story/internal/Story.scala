package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Narrative

class Story(val title: String = "Untitled",
            val author: String = "",
            val description: String = "", 
            val nodes: List[Node] = Nil) extends Narrative {
  def rename(newTitle: String) = new Story(newTitle, author, description, nodes)
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, nodes)
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, nodes)

  def addNode(node: Node) = new Story(title, author, description, node :: nodes)
  def updateNode(node: Node, newNode: Node) = new Story(title, author, description, newNode :: nodes filter (_ != node))
  def removeNode(node: Node) = new Story(title, author, description, nodes filter (_ != node))
}
