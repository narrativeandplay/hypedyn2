package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.api.story.Narrative
import org.narrativeandplay.hypedyn.api.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.api.story.Narrative.ReaderStyle.Standard
import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.story.rules.internal.Rule

/**
 * Class representing a story. The story class is immutable, and all operations on the story return a new copy
 *
 * @param metadata The metadata of the story
 * @param nodes The story's nodes
 * @param facts The story's facts
 * @param rules The story-level rules
 */
case class Story(metadata: Story.Metadata = Story.Metadata(),
                 nodes: List[Node] = Nil,
                 facts: List[Fact] = Nil,
                 rules: List[Rule] = Nil) extends Narrative {
  /**
   * Updates the metadata of the story
   *
   * @param newMetadata The new metadata of the story
   * @return A new story with the metadata changed
   */
  def updateMetadata(newMetadata: Story.Metadata) = new Story(newMetadata, nodes, facts, rules)

  /**
   * Adds a node to the story
   *
   * @param node The node to add to the story
   * @return A new story with the specified node added
   */
  def addNode(node: Node) = new Story(metadata, node :: nodes, facts, rules)

  /**
   * Updates a node of the story
   *
   * @param node The node to update
   * @param newNode The updated version of the node
   * @return A new story with the specified node updated
   */
  def updateNode(node: Node, newNode: Node) =
    new Story(metadata, newNode :: (nodes filter (_ != node)), facts, rules)

  /**
   * Removes a node from the story
   * @param node The node to remove
   * @return A new story with the specified node removed
   */
  def removeNode(node: Node) = new Story(metadata, nodes filter (_ != node), facts, rules)

  /**
   * Adds a fact to the story
   *
   * @param fact The fact to add
   * @return A new story with the specified fact added
   */
  def addFact(fact: Fact) = new Story(metadata, nodes, fact :: facts, rules)

  /**
   * Updates a fact of the story
   *
   * @param fact The fact to update
   * @param newFact The updated version of the fact
   * @return A new story with the specified fact updated
   */
  def updateFact(fact: Fact, newFact: Fact) = new Story(metadata, nodes,
                                                        newFact :: (facts filter (_ != fact)), rules)

  /**
   * Removes a fact from the story
   *
   * @param fact The fact to remove
   * @return A new story with the specified fact removed
   */
  def removeFact(fact: Fact) = new Story(metadata, nodes, facts filter (_ != fact), rules)

  /**
   * Adds a story-level rule
   *
   * @param rule The rule to add
   * @return A new story with the specified fact added
   */
  def addRule(rule: Rule) = new Story(metadata, nodes, facts, rule :: rules)

  /**
   * Updates a story-level rule
   *
   * @param rule The rule to update
   * @param newRule The updated version of the rule
   * @return A new story with the specified rule updated
   */
  def updateRule(rule: Rule, newRule: Rule) = new Story(metadata, nodes, facts,
                                                        newRule :: (rules filter (_ != rule)))

  /**
   * Removes a story-level rule
   *
   * @param rule The rule to remove
   * @return A new story with the specified rule removed
   */
  def removeRule(rule: Rule) = new Story(metadata, nodes, facts, rules filter (_ != rule))

  /**
   * Returns all the rules in the story (text, node, and story rules)
   */
  def allRules = rules ++ (nodes flatMap (_.rules)) ++ (nodes flatMap (_.content.rulesets) flatMap (_.rules))

  /**
   * Returns the start node of the story if it has one, or None if the start node has not yet been set
   */
  def startNode = nodes find (_.isStartNode)
}

object Story {

  /**
   * Class to represent story metadata
   *
   * @param title The title of the story
   * @param author The author of the story
   * @param description The description of the story
   * @param comments The story's comments
   * @param readerStyle The style of the reader
   * @param isBackButtonDisabled Whether the back button is disabled
   * @param isRestartButtonDisabled Whether the restart button is disabled
   */
  case class Metadata(title: String = "Untitled",
                      author: String = "",
                      description: String = "",
                      comments: String = "",
                      readerStyle: ReaderStyle = Standard,
                      isBackButtonDisabled: Boolean = false,
                      isRestartButtonDisabled: Boolean = false) extends Narrative.Metadata
}
