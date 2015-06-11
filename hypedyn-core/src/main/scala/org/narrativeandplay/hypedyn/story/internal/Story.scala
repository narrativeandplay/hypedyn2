package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Narrative
import org.narrativeandplay.hypedyn.story.rules.internal.Rule
import org.narrativeandplay.hypedyn.story.rules.Fact

case class Story(title: String = "Untitled",
                 author: String = "",
                 description: String = "",
                 nodes: List[Node] = Nil,
                 facts: List[Fact] = Nil,
                 rules: List[Rule] = Nil) extends Narrative {
  def rename(newTitle: String) = new Story(newTitle, author, description, nodes, facts, rules)
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, nodes, facts, rules)
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, nodes, facts, rules)

  def addNode(node: Node) = new Story(title, author, description, node :: nodes, facts, rules)
  def updateNode(node: Node, newNode: Node) = new Story(title, author, description,
                                                        newNode :: (nodes filter (_ != node)), facts, rules)
  def removeNode(node: Node) = new Story(title, author, description, nodes filter (_ != node), facts, rules)

  def addRule(rule: Rule) = new Story(title, author, description, nodes, facts, rule :: rules)
  def updateRule(rule: Rule, newRule: Rule) = new Story(title, author, description, nodes, facts,
                                                        newRule :: (rules filter (_ != rule)))
  def removeRule(rule: Rule) = new Story(title, author, description, nodes, facts, rules filter (_ != rule))
}
