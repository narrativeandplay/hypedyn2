package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.ParamValue
import org.narrativeandplay.hypedyn.api.story.{Narrative, Nodal, NodalContent, NodeId}
import org.narrativeandplay.hypedyn.api.story.rules._
import org.narrativeandplay.hypedyn.story.internal.Story.Metadata
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.InterfaceToImplementationConversions._


/**
 * Controller for handling story-related actions
 */
object StoryController {
  import Ordering.Implicits._

  private var currentStory = new Story()
  private var firstUnusedNodeId = NodeId(0)
  private var firstUnusedFactId = FactId(0)
  private var firstUnusedRuleId = RuleId(0)
  private var firstUnusedRulesetId = NodalContent.RulesetId(0)

  /**
   * Returns the story currently being edited
   */
  def story = currentStory

  /**
   * Sets up a new story
   *
   * @param title The title of the new story
   * @param author The author of the new story
   * @param description The description of the new story
   */
  def newStory(title: String, author: String, description: String): Unit = {
    load(new Story(Metadata(title, author, description)))
  }

  /**
   * Edit the current story
   *
   * @param metadata The new metadata
   */
  def editStory(metadata: Narrative.Metadata): Unit = {
    currentStory = currentStory updateMetadata metadata
  }

  /**
   * Load a given story
   *
   * @param story The story to load
   */
  def load(story: Story): Unit = {
    currentStory = story
    firstUnusedNodeId = story.nodes map (_.id) reduceOption (_ max _) map (_.inc) getOrElse NodeId(0)
    firstUnusedFactId = story.facts map (_.id) reduceOption (_ max _) map (_.inc) getOrElse FactId(0)
    firstUnusedRuleId = story.allRules map (_.id) reduceOption (_ max _) map (_.inc) getOrElse RuleId(0)
    firstUnusedRulesetId = story.nodes flatMap (_.content.rulesets) map (_.id) reduceOption (_ max _) map (_.inc) getOrElse NodalContent.RulesetId(0)
  }

  /**
   * Finds a node given a node ID
   *
   * @param nodeId The ID of the node to find
   * @return An option containing the found node, or None if no node with the given ID exists
   */
  def findNode(nodeId: NodeId) = currentStory.nodes find (_.id == nodeId)

  /**
   * Finds a fact given a fact ID
   *
   * @param factId The ID of the fact to find
   * @return An option containing the found node or None if no fact with the given ID exists
   */
  def findFact(factId: FactId) = currentStory.facts find (_.id == factId)

  /**
   * Create a node
   *
   * @param node The data for the node to create
   * @return The created node
   */
  def create(node: Nodal): (Node, Map[Node, Node]) = {
    val newNodeContent = NodeContent(node.content.text, node.content.rulesets map { rulesetLike =>
      val ruleset = rulesetLike.copy(id = if (rulesetLike.id.isValid) rulesetLike.id else firstUnusedRulesetId,
                                     rules = rulesetLike.rules map { rule =>
                                       val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
                                       firstUnusedRuleId = firstUnusedRuleId max r.id.inc
                                       r
                                     })
      firstUnusedRulesetId = firstUnusedRulesetId max ruleset.id.inc
      ruleset
    })
    val newNodeRules = node.rules map { rule =>
      val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
      firstUnusedRuleId = firstUnusedRuleId max r.id.inc
      r
    }
    val newNode = Node(if (node.id.isValid) node.id else firstUnusedNodeId,
                       node.name, newNodeContent, node.isStartNode, newNodeRules)

    val oldStartNodeChange = if (node.isStartNode) {
      List(currentStory.startNode flatMap { oldStartNode =>
        updateNode(oldStartNode, oldStartNode.copy(isStartNode = false))
      }).flatten
    } else List.empty

    currentStory = currentStory addNode newNode
    firstUnusedNodeId = firstUnusedNodeId max newNode.id.inc

    (newNode, oldStartNodeChange.toMap)
  }

  /**
   * Update a node
   *
   * @param node The node to update
   * @param editedNode The updated node data
   * @return An option containing the original node, the updated node, and an option containing the unchanged and
   *         changed node if the node to update was made the start node, or None if the node to update did not exist
   */
  def update(node: Nodal, editedNode: Nodal): Option[(Node, Node, Option[(Node, Node)])] = {
    val updatedOldStartNodeOption = if (editedNode.isStartNode && !node.isStartNode) {
      currentStory.startNode flatMap { oldStartNode =>
        updateNode(oldStartNode, oldStartNode.copy(isStartNode = false))
      }
    } else None

    val updatedNode = updateNode(node, editedNode)

    updatedNode map { case (unupdated, updated) => (unupdated, updated, updatedOldStartNodeOption) }
  }

  /**
   * Updates a node
   *
   * @param node The node to update
   * @param updatedNode The updated node data
   * @return An option containing the unupdated and updated versions of the node, or None if the node to update
   *         does not exist
   */
  private def updateNode(node: Nodal, updatedNode: Nodal): Option[(Node, Node)] = {
    val editedNodeContent = NodeContent(updatedNode.content.text, updatedNode.content.rulesets map { rulesetLike =>
      val ruleset = rulesetLike.copy(id = if (rulesetLike.id.isValid) rulesetLike.id else firstUnusedRulesetId,
                                     rules = rulesetLike.rules map { rule =>
                                       val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
                                       firstUnusedRuleId = firstUnusedRuleId max r.id.inc
                                       r
                                     })
      firstUnusedRulesetId = firstUnusedRulesetId max ruleset.id.inc
      ruleset
    })
    val updatedNodeRules = updatedNode.rules map { rule =>
      val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
      firstUnusedRuleId = firstUnusedRuleId max r.id.inc
      r
    }

    val toUpdateOption = findNode(node.id)
    val updated = updatedNode.copy(content = editedNodeContent)

    toUpdateOption foreach { n =>
      currentStory = currentStory updateNode (n, updated)
    }

    toUpdateOption map ((_, updated))
  }

  /**
   * Destroy a node
   *
   * @param node The node to destroy
   * @return An option containing the destroyed node and a map of nodes changed as a result of the destroyed node,
   *         or None if the node to destroy does not exist
   */
  def destroy(node: Nodal): Option[(Node, Map[Node, Node])] = {
    val toDestroyOption = findNode(node.id)

    toDestroyOption foreach { toDestroy => currentStory = currentStory removeNode toDestroy }
    val destroyedNodeChangedNodesOption = toDestroyOption map { toDestroy =>
      val nodesToEdit = currentStory.nodes filter { node =>
        val nodeActionsReferenceToDestroy = (for {
          actions <- node.rules map (_.actions)
          parameterValues <- actions map (_.params.values)
          nodeParameterValue <- parameterValues collect { case p: ParamValue.Node => p }
        } yield nodeParameterValue.node) contains toDestroy.id
        val nodeConditionsReferenceToDestroy = (for {
          conditions <- node.rules map (_.conditions)
          parameterValues <- conditions map (_.params.values)
          nodeParameterValue <- parameterValues collect { case p: ParamValue.Node => p }
        } yield nodeParameterValue.node) contains toDestroy.id

        val textRules = node.content.rulesets flatMap (_.rules)
        val textRulesActionsReferenceToDestroy = (for {
          actions <- textRules map (_.actions)
          parameterValues <- actions map (_.params.values)
          nodeParameterValue <- parameterValues collect { case p: ParamValue.Node => p }
        } yield nodeParameterValue.node) contains toDestroy.id
        val textRulesConditionsReferenceToDestroy = (for {
          conditions <- textRules map (_.conditions)
          parameterValues <- conditions map (_.params.values)
          nodeParameterValue <- parameterValues collect { case p: ParamValue.Node => p }
        } yield nodeParameterValue.node) contains toDestroy.id

        nodeActionsReferenceToDestroy || nodeConditionsReferenceToDestroy || textRulesActionsReferenceToDestroy || textRulesConditionsReferenceToDestroy
      }

      val editedNodePairs = nodesToEdit map { node =>
        val modifiedNodeRules = node.rules map { rule =>
          val modifiedActions = rule.actions filterNot { action => action.params.values.toList contains ParamValue.Node(toDestroy.id) }
          val modifiedConditions = rule.conditions filterNot { action => action.params.values.toList contains ParamValue.Node(toDestroy.id) }

          rule.copy(conditions = modifiedConditions, actions = modifiedActions)
        }

        val modifiedRulesets = node.content.rulesets map { ruleset =>
          val modifiedRules = ruleset.rules map { rule =>
            val modifiedActions = rule.actions filterNot { action => action.params.values.toList contains ParamValue.Node(toDestroy.id) }
            val modifiedConditions = rule.conditions filterNot { action => action.params.values.toList contains ParamValue.Node(toDestroy.id) }

            rule.copy(conditions = modifiedConditions, actions = modifiedActions)
          }

          ruleset.copy(rules = modifiedRules)
        }

        node -> node.copy(content = node.content.copy(rulesets = modifiedRulesets), rules = modifiedNodeRules)
      }

      editedNodePairs foreach { case (unedited, edited) =>
        update(unedited, edited)
      }

      (toDestroy, editedNodePairs.toMap)
    }

    destroyedNodeChangedNodesOption
  }

  /**
   * Create a new fact in the story
   *
   * @param fact The new fact
   * @return The created fact
   */
  def create(fact: Fact): Fact = {
    val newFact = instantiateFact(fact)

    currentStory = currentStory addFact newFact

    newFact
  }

  /**
   * Update a fact
   *
   * @param fact The fact to update
   * @param editedFact The updated fact data
   * @return An option containing the unupdated and updated versions of the fact, or None if the fact to update
   *         does not exist
   */
  def update(fact: Fact, editedFact: Fact): Option[(Fact, Fact)] = {
    val toUpdate = findFact(fact.id)
    val updated = instantiateFact(editedFact)

    toUpdate foreach { f => currentStory = currentStory updateFact (f, updated) }

    toUpdate map ((_, updated))
  }

  /**
   * Destroy a fact
   *
   * @param fact The fact to destroy
   * @return An option containing the destroyed fact, or None if the fact to destroy does not exist
   */
  def destroy(fact: Fact): Option[Fact] = {
    val toDestroy = findFact(fact.id)

    toDestroy foreach { f => currentStory = currentStory removeFact f }

    toDestroy
  }

  /**
   * Instantiates a fact, i.e., provides a fact with a valid ID
   *
   * @param fact The fact to instantiate
   * @return The instantiated fact
   */
  private def instantiateFact(fact: Fact): Fact = {
    val factInstance = fact match {
      case IntegerFact(id, name, initVal) => IntegerFact(if (id.isValid) id else firstUnusedFactId,
                                                         name, initVal)
      case StringFact(id, name, initVal) => StringFact(if (id.isValid) id else firstUnusedFactId,
                                                       name, initVal)
      case BooleanFact(id, name, initVal) => BooleanFact(if (id.isValid) id else firstUnusedFactId,
                                                         name, initVal)
      case IntegerFactList(id, name, initVal) =>
        val actualId = if (id.isValid) id else firstUnusedFactId
        firstUnusedFactId = firstUnusedFactId max actualId.inc
        IntegerFactList(actualId,
                        name,
                        (initVal map instantiateFact).asInstanceOf[List[IntegerFact]])
      case StringFactList(id, name, initVal) =>
        val actualId = if (id.isValid) id else firstUnusedFactId
        firstUnusedFactId = firstUnusedFactId max actualId.inc
        StringFactList(actualId,
                       name,
                       (initVal map instantiateFact).asInstanceOf[List[StringFact]])
      case BooleanFactList(id, name, initVal) =>
        val actualId = if (id.isValid) id else firstUnusedFactId
        firstUnusedFactId = firstUnusedFactId max actualId.inc
        BooleanFactList(actualId,
                        name,
                        (initVal map instantiateFact).asInstanceOf[List[BooleanFact]])
    }

    firstUnusedFactId = firstUnusedFactId max factInstance.id.inc

    factInstance
  }

}
