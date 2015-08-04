package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.internal.NodeContent.Ruleset
import org.narrativeandplay.hypedyn.story.internal.{NodeContent, Node, Story}
import org.narrativeandplay.hypedyn.story.InterfaceToImplementationConversions._
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamValue, ParamName}
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.rules.internal.Rule

object StoryController {
  import Ordering.Implicits._

  private var currentStory = new Story()
  private var firstUnusedNodeId = NodeId(0)
  private var firstUnusedFactId = FactId(0)
  private var firstUnusedRuleId = RuleId(0)
  private var firstUnusedRulesetId = NodalContent.RulesetId(0)

  def story = currentStory

  def newStory(title: String, author: String, description: String): Unit = {
    currentStory = new Story(title, author, description)
  }

  def editStory(title: String, author: String, description: String): Unit = {
    currentStory = currentStory rename title
    currentStory = currentStory changeAuthor author
    currentStory = currentStory changeDescription description
  }

  def load(story: Story): Unit = {
    currentStory = story
    firstUnusedNodeId = story.nodes map (_.id) reduceOption (_ max _) map (_.inc) getOrElse NodeId(0)
    firstUnusedFactId = story.facts map (_.id) reduceOption (_ max _) map (_.inc) getOrElse FactId(0)
    firstUnusedRuleId = story.allRules map (_.id) reduceOption (_ max _) map (_.inc) getOrElse RuleId(0)
    firstUnusedRulesetId = story.nodes flatMap (_.content.rulesets) map (_.id) reduceOption (_ max _) map (_.inc) getOrElse NodalContent.RulesetId(0)
  }

  def findNode(nodeId: NodeId) = currentStory.nodes find (_.id == nodeId)
  def findFact(factId: FactId) = currentStory.facts find (_.id == factId)

  def create(node: Nodal): Node = {
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
    currentStory = currentStory addNode newNode
    firstUnusedNodeId = firstUnusedNodeId max newNode.id.inc

    newNode
  }

  def update(node: Nodal, editedNode: Nodal): Option[(Node, Node)] = {
    val editedNodeContent = NodeContent(editedNode.content.text, editedNode.content.rulesets map { rulesetLike =>
      val ruleset = rulesetLike.copy(id = if (rulesetLike.id.isValid) rulesetLike.id else firstUnusedRulesetId,
                                     rules = rulesetLike.rules map { rule =>
                                       val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
                                       firstUnusedRuleId = firstUnusedRuleId max r.id.inc
                                       r
                                     })
      firstUnusedRulesetId = firstUnusedRulesetId max ruleset.id.inc
      ruleset
    })
    val editedNodeRules = editedNode.rules map { rule =>
      val r = rule.copy(id = if (rule.id.isValid) rule.id else firstUnusedRuleId)
      firstUnusedRuleId = firstUnusedRuleId max r.id.inc
      r
    }

    val toUpdate = findNode(node.id)
    val updated = new Node(editedNode.id, editedNode.name, editedNodeContent, editedNode.isStartNode, editedNodeRules)

    toUpdate foreach { n => currentStory = currentStory updateNode (n, updated) }

    toUpdate map ((_, updated))
  }

  def destroy(node: Nodal): Option[(Node, Map[Node, Node])] = {
    val toDestroyOption = findNode(node.id)

    toDestroyOption foreach { toDestroy => currentStory = currentStory removeNode toDestroy }
    val retVal = toDestroyOption map { toDestroy =>
      val nodesToEdit = currentStory.nodes filter { node =>
        val nodeActionsReferenceToDestroy = node.rules flatMap (_.actions) flatMap (_.params get ParamName("node")) contains ParamValue(toDestroy.id.value.toString())
        val nodeConditionsReferenceToDestroy = node.rules flatMap (_.conditions) flatMap (_.params get ParamName("node")) contains ParamValue(toDestroy.id.value.toString())

        val textRules = node.content.rulesets flatMap (_.rules)
        val textRulesActionsReferenceToDestroy = textRules flatMap (_.actions) flatMap (_.params get ParamName("node")) contains ParamValue(toDestroy.id.value.toString())
        val textRulesConditionsReferenceToDestroy = textRules flatMap (_.conditions) flatMap (_.params get ParamName("node")) contains ParamValue(toDestroy.id.value.toString())

        nodeActionsReferenceToDestroy || nodeConditionsReferenceToDestroy || textRulesActionsReferenceToDestroy || textRulesConditionsReferenceToDestroy
      }

      val editedNodePairs = nodesToEdit map { node =>
        val modifiedNodeRules = node.rules map { rule =>
          val modifiedActions = rule.actions filterNot { action => action.params.values.toList contains ParamValue(toDestroy.id.value.toString()) }
          val modifiedConditions = rule.conditions filterNot { action => action.params.values.toList contains ParamValue(toDestroy.id.value.toString()) }

          new Rule(rule.id, rule.name, rule.conditionsOp, modifiedConditions, modifiedActions)
        }

        val modifiedRulesets = node.content.rulesets map { ruleset =>
          val modifiedRules = ruleset.rules map { rule =>
            val modifiedActions = rule.actions filterNot { action => action.params.values.toList contains ParamValue(toDestroy.id.value.toString()) }
            val modifiedConditions = rule.conditions filterNot { action => action.params.values.toList contains ParamValue(toDestroy.id.value.toString()) }

            new Rule(rule.id, rule.name, rule.conditionsOp, modifiedConditions, modifiedActions)
          }

          new Ruleset(ruleset.id, ruleset.name, ruleset.indexes, modifiedRules)
        }

        node -> new Node(node.id, node.name, new NodeContent(node.content.text, modifiedRulesets), node.isStartNode, modifiedNodeRules)
      }

      editedNodePairs foreach { case (unedited, edited) =>
        update(unedited, edited)
      }

      (toDestroy, editedNodePairs.toMap)
    }

    retVal
  }

  def create(fact: Fact): Fact = {
    val newFact = instantiateFact(fact)

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
