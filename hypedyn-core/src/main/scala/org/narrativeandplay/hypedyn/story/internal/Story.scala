package org.narrativeandplay.hypedyn.story.internal

import org.narrativeandplay.hypedyn.story.Narrative
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle.Standard
import org.narrativeandplay.hypedyn.story.rules.internal.Rule
import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThemeLike}

/**
 * Class representing a story. The story class is immutable, and all operations on the story return a new copy
 *
 * @param title The title of the story
 * @param author The author of the story
 * @param description The description of the story
 * @param metadata The metadata of the story
 * @param nodes The story's nodes
 * @param facts The story's facts
 * @param rules The story-level rules
 * @param themes The story's themes
 * @param motifs The story's motifs
 */
case class Story(title: String = "Untitled",
                 author: String = "",
                 description: String = "",
                 metadata: Story.Metadata = Story.Metadata(),
                 nodes: List[Node] = Nil,
                 facts: List[Fact] = Nil,
                 rules: List[Rule] = Nil,
                 themes: List[ThemeLike] = Nil,
                 motifs: List[MotifLike] = Nil) extends Narrative {
  /**
   * Changes the title of the story
   *
   * @param newTitle The new title of the story
   * @return A new story with the title changed
   */
  def rename(newTitle: String) = new Story(newTitle, author, description, metadata, nodes, facts, rules, themes, motifs)

  /**
   * Changes the author of the story
   *
   * @param newAuthor The new author of the story
   * @return A new story with the author changed
   */
  def changeAuthor(newAuthor: String) = new Story(title, newAuthor, description, metadata, nodes, facts, rules, themes, motifs)

  /**
   * Changes the description of the story
   *
   * @param newDescription The new description of the story
   * @return A new story with the description changed
   */
  def changeDescription(newDescription: String) = new Story(title, author, newDescription, metadata, nodes, facts, rules, themes, motifs)

  /**
   * Updates the metadata of the story
   *
   * @param newMetadata The new metadata of the story
   * @return A new story with the metadata changed
   */
  def updateMetadata(newMetadata: Story.Metadata) = new Story(title, author, description, newMetadata, nodes, facts, rules, themes, motifs)

  /**
   * Adds a node to the story
   *
   * @param node The node to add to the story
   * @return A new story with the specified node added
   */
  def addNode(node: Node) = new Story(title, author, description, metadata, node :: nodes, facts, rules, themes, motifs)

  /**
   * Updates a node of the story
   *
   * @param node The node to update
   * @param newNode The updated version of the node
   * @return A new story with the specified node updated
   */
  def updateNode(node: Node, newNode: Node) = new Story(title, author, description, metadata,
                                                        newNode :: (nodes filter (_ != node)), facts, rules, themes, motifs)

  /**
   * Removes a node from the story
    *
    * @param node The node to remove
   * @return A new story with the specified node removed
   */
  def removeNode(node: Node) = new Story(title, author, description, metadata, nodes filter (_ != node), facts, rules, themes, motifs)

  /**
   * Adds a fact to the story
   *
   * @param fact The fact to add
   * @return A new story with the specified fact added
   */
  def addFact(fact: Fact) = new Story(title, author, description, metadata, nodes, fact :: facts, rules, themes, motifs)

  /**
   * Updates a fact of the story
   *
   * @param fact The fact to update
   * @param newFact The updated version of the fact
   * @return A new story with the specified fact updated
   */
  def updateFact(fact: Fact, newFact: Fact) = new Story(title, author, description, metadata, nodes,
                                                        newFact :: (facts filter (_ != fact)), rules, themes, motifs)

  /**
   * Removes a fact from the story
   *
   * @param fact The fact to remove
   * @return A new story with the specified fact removed
   */
  def removeFact(fact: Fact) = new Story(title, author, description, metadata, nodes, facts filter (_ != fact), rules, themes, motifs)

  /**
   * Adds a story-level rule
   *
   * @param rule The rule to add
   * @return A new story with the specified fact added
   */
  def addRule(rule: Rule) = new Story(title, author, description, metadata, nodes, facts, rule :: rules, themes, motifs)

  /**
   * Updates a story-level rule
   *
   * @param rule The rule to update
   * @param newRule The updated version of the rule
   * @return A new story with the specified rule updated
   */
  def updateRule(rule: Rule, newRule: Rule) = new Story(title, author, description, metadata, nodes, facts,
                                                        newRule :: (rules filter (_ != rule)), themes, motifs)

  /**
   * Removes a story-level rule
   *
   * @param rule The rule to remove
   * @return A new story with the specified rule removed
   */
  def removeRule(rule: Rule) = new Story(title, author, description, metadata, nodes, facts, rules filter (_ != rule), themes, motifs)

  /**
   * Returns all the rules in the story (text, node, and story rules)
   */
  def allRules = rules ++ (nodes flatMap (_.rules)) ++ (nodes flatMap (_.content.rulesets) flatMap (_.rules))

  /**
    * Adds a theme to the story
    *
    * @param theme The theme to add
    * @return A new story with the specified theme added
    */
  def addTheme(theme: ThemeLike) = new Story(title, author, description, metadata, nodes, facts, rules, theme :: themes, motifs)

  /**
    * Updates a theme of the story
    *
    * @param theme The fact to update
    * @param newTheme The updated version of the theme
    * @return A new story with the specified theme updated
    */
  def updateTheme(theme: ThemeLike, newTheme: ThemeLike) = new Story(title, author, description, metadata, nodes,
    facts, rules, newTheme :: (themes filter (_ != theme)), motifs)

  /**
    * Removes a motif from the story
    *
    * @param motif The motif to remove
    * @return A new story with the specified theme removed
    */
  def removeMotif(motif: MotifLike) = new Story(title, author, description, metadata, nodes, facts, rules, themes, motifs filter (_ != motif))

  /**
    * Adds a motif to the story
    *
    * @param motif The motif to add
    * @return A new story with the specified theme added
    */
  def addMotif(motif: MotifLike) = new Story(title, author, description, metadata, nodes, facts, rules, themes, motif :: motifs)

  /**
    * Updates a motif of the story
    *
    * @param motif The motif to update
    * @param newMotif The updated version of the motif
    * @return A new story with the specified motif updated
    */
  def updateTheme(motif: MotifLike, newMotif: MotifLike) = new Story(title, author, description, metadata, nodes,
    facts, rules, themes, newMotif :: (motifs filter (_ != motif)))

  /**
    * Removes a theme from the story
    *
    * @param theme The theme to remove
    * @return A new story with the specified theme removed
    */
  def removeTheme(theme: ThemeLike) = new Story(title, author, description, metadata, nodes, facts, rules, themes filter (_ != theme))

  /**
   * Returns the start node of the story if it has one, or None if the start node has not yet been set
   */
  def startNode = nodes find (_.isStartNode)
}

object Story {

  /**
   * Class to represent story metadata
   *
   * @param comments The story's comments
   * @param readerStyle The style of the reader
   * @param isBackButtonDisabled Whether the back button is disabled
   * @param isRestartButtonDisabled Whether the restart button is disabled
   */
  case class Metadata(comments: String = "",
                      readerStyle: ReaderStyle = Standard,
                      isBackButtonDisabled: Boolean = false,
                      isRestartButtonDisabled: Boolean = false) extends Narrative.Metadata
}
