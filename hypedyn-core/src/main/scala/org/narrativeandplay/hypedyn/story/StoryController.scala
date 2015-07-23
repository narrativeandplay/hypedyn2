package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.internal.{NodeContent, Node, Story}
import org.narrativeandplay.hypedyn.story.InterfaceToImplementationConversions._
import org.narrativeandplay.hypedyn.story.rules._

object StoryController {
  private var currentStory = new Story()
  private var firstUnusedNodeId = NodeId(0)
  private var firstUnusedFactId = FactId(0)
  private var firstUnusedRuleId = RuleId(0)
  private var firstUnusedRulesetId = NodalContent.RulesetId(0)

  def story = currentStory

  def newStory(title: String, author: String, description: String): Unit = {
    currentStory = new Story(title, author, description)
  }

  def load(story: Story): Unit = {
    currentStory = story
    firstUnusedNodeId = (story.nodes map (_.id)).max.inc
  }

  def findNode(nodeId: NodeId) = currentStory.nodes find (_.id == nodeId)
  def findFact(factId: FactId) = currentStory.facts find (_.id == factId)

  def create(node: Nodal): Node = {
    val newNodeContent = NodeContent(node.content.text, node.content.rulesets map { rulesetLike =>
      val ruleset = rulesetLike.copy(id = if (rulesetLike.id.isValid) rulesetLike.id else firstUnusedRulesetId,
                                     rules = rulesetLike.rules map { rule =>
                                       val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
                                       firstUnusedRuleId = List(firstUnusedRuleId, r.id.inc).max
                                       r
                                     })
      firstUnusedRulesetId = List(firstUnusedRulesetId, ruleset.id.inc).max
      ruleset
    })
    val newNodeRules = node.rules map { rule =>
      val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
      firstUnusedRuleId = List(firstUnusedRuleId, r.id.inc).max
      r
    }
    val newNode = Node(if (node.id.isValid) node.id else firstUnusedNodeId,
                       node.name, newNodeContent, node.isStartNode, newNodeRules)
    currentStory = currentStory addNode newNode
    firstUnusedNodeId = List(firstUnusedNodeId, newNode.id.inc).max

    newNode
  }

  def update(node: Nodal, editedNode: Nodal): Option[(Node, Node)] = {
    val editedNodeContent = NodeContent(editedNode.content.text, editedNode.content.rulesets map { rulesetLike =>
      val ruleset = rulesetLike.copy(id = if (rulesetLike.id.isValid) rulesetLike.id else firstUnusedRulesetId,
                                     rules = rulesetLike.rules map { rule =>
                                       val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
                                       firstUnusedRuleId = List(firstUnusedRuleId, r.id.inc).max
                                       r
                                     })
      firstUnusedRulesetId = List(firstUnusedRulesetId, ruleset.id.inc).max
      ruleset
    })
    val editedNodeRules = editedNode.rules map { rule =>
      val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
      firstUnusedRuleId = List(firstUnusedRuleId, r.id.inc).max
      r
    }

    val toUpdate = findNode(node.id)
    val updated = new Node(editedNode.id, editedNode.name, editedNodeContent, editedNode.isStartNode, editedNodeRules)

    toUpdate foreach { n => currentStory = currentStory updateNode (n, updated) }

    toUpdate map ((_, updated))
  }

  def destroy(node: Nodal): Option[Node] = {
    val toDestroy = findNode(node.id)

    toDestroy foreach { n => currentStory = currentStory removeNode n }

    toDestroy
  }

  def create(fact: Fact): Fact = {
    val newFact: Fact = instantiateFact(fact)

    firstUnusedFactId = List(newFact.id.inc, firstUnusedFactId).max

    currentStory = currentStory addFact newFact

    newFact
  }

  def update(fact: Fact, editedFact: Fact): Option[(Fact, Fact)] = {
    val toUpdate = findFact(fact.id)
    val updated = instantiateFact(editedFact)

    toUpdate foreach { f => currentStory = currentStory updateFact (f, updated) }

    toUpdate map ((_, updated))
  }

  def destroy(fact: Fact): Option[Fact] = {
    val toDestroy = findFact(fact.id)

    toDestroy foreach { f => currentStory = currentStory removeFact f }

    toDestroy
  }

  private def instantiateFact(fact: Fact): Fact = {
    val factInstance = fact match {
      case IntegerFact(id, name, initVal) => IntegerFact(if (id.isValid) id else firstUnusedFactId,
                                                         name, initVal)
      case StringFact(id, name, initVal) => StringFact(if (id.isValid) id else firstUnusedFactId,
                                                       name, initVal)
      case BooleanFact(id, name, initVal) => BooleanFact(if (id.isValid) id else firstUnusedFactId,
                                                         name, initVal)
      case IntegerFactList(id, name, initVal) =>
        IntegerFactList(if (id.isValid) id else firstUnusedFactId,
                        name,
                        (initVal map instantiateFact).asInstanceOf[List[IntegerFact]])
      case StringFactList(id, name, initVal) =>
        StringFactList(if (id.isValid) id else firstUnusedFactId,
                       name,
                       (initVal map instantiateFact).asInstanceOf[List[StringFact]])
      case BooleanFactList(id, name, initVal) =>
        BooleanFactList(if (id.isValid) id else firstUnusedFactId,
                        name,
                        (initVal map instantiateFact).asInstanceOf[List[BooleanFact]])
    }

    firstUnusedFactId = List(factInstance.id.inc, firstUnusedFactId).max

    factInstance
  }

}
