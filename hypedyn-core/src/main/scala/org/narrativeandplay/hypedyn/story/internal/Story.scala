package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Narrative
import org.narrativeandplay.hypedyn.story.rules.Rule

case class Story(title: String = "Untitled",
                 author: String = "",
                 description: String = "",
                 nodes: List[Node] = Nil,
                 rules: List[Rule] = Nil) extends Narrative {
  def rename(newTitle: String) = new Story(newTitle, author, description, nodes, rules)
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, nodes, rules)
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, nodes, rules)

  def addNode(node: Node) = new Story(title, author, description, node :: nodes, rules)
  def updateNode(node: Node, newNode: Node) = new Story(title, author, description,
                                                        newNode :: (nodes filter (_ != node)), rules)
  def removeNode(node: Node) = new Story(title, author, description, nodes filter (_ != node), rules)

  def addRule(rule: Rule) = new Story(title, author, description, nodes, rule :: rules)
  def updateRule(rule: Rule, newRule: Rule) = new Story(title, author, description, nodes,
                                                                              newRule :: (rules filter (_ != rule)))
  def removeRule(rule: Rule) = new Story(title, author, description, nodes, rules filter (_ != rule))
}
