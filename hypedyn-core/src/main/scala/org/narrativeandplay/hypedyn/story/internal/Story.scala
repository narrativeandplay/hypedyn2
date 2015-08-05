package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Narrative
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle.Standard
import org.narrativeandplay.hypedyn.story.rules.internal.Rule
import org.narrativeandplay.hypedyn.story.rules.Fact

case class Story(title: String = "Untitled",
                 author: String = "",
                 description: String = "",
                 metadata: Story.Metadata = Story.Metadata(),
                 nodes: List[Node] = Nil,
                 facts: List[Fact] = Nil,
                 rules: List[Rule] = Nil) extends Narrative {
  def rename(newTitle: String) = new Story(newTitle, author, description, metadata, nodes, facts, rules)
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, metadata, nodes, facts, rules)
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, metadata, nodes, facts, rules)
  def updateMetadata(newMetadata: Story.Metadata) = new Story(title, author, description, newMetadata, nodes, facts, rules)

  def addNode(node: Node) = new Story(title, author, description, metadata, node :: nodes, facts, rules)
  def updateNode(node: Node, newNode: Node) = new Story(title, author, description, metadata,
                                                        newNode :: (nodes filter (_ != node)), facts, rules)
  def removeNode(node: Node) = new Story(title, author, description, metadata, nodes filter (_ != node), facts, rules)

  def addFact(fact: Fact) = new Story(title, author, description, metadata, nodes, fact :: facts, rules)
  def updateFact(fact: Fact, newFact: Fact) = new Story(title, author, description, metadata, nodes,
                                                        newFact :: (facts filter (_ != fact)), rules)
  def removeFact(fact: Fact) = new Story(title, author, description, metadata, nodes, facts filter (_ != fact), rules)

  def addRule(rule: Rule) = new Story(title, author, description, metadata, nodes, facts, rule :: rules)
  def updateRule(rule: Rule, newRule: Rule) = new Story(title, author, description, metadata, nodes, facts,
                                                        newRule :: (rules filter (_ != rule)))
  def removeRule(rule: Rule) = new Story(title, author, description, metadata, nodes, facts, rules filter (_ != rule))

  def allRules = rules ++ (nodes flatMap (_.rules)) ++ (nodes flatMap (_.content.rulesets) flatMap (_.rules))
}

object Story {
  case class Metadata(comments: String = "",
                      readerStyle: ReaderStyle = Standard,
                      isBackButtonDisabled: Boolean = false,
                      isRestartButtonDisabled: Boolean = false) extends Narrative.Metadata
}
