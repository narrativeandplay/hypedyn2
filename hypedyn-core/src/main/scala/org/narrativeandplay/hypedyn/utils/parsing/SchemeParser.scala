package org.narrativeandplay.hypedyn.utils.parsing

import org.narrativeandplay.hypedyn.events._
import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetId, RulesetIndexes, TextIndex}
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.story.internal.NodeContent.Ruleset
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamValue.{StringInput, SelectedListValue, UnionValueSelected}
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamValue, ParamName}
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, _}
import org.narrativeandplay.hypedyn.story.rules.{BooleanOperator, _}

import scala.collection.mutable
import scala.util.parsing.combinator.JavaTokenParsers

object SchemeParser extends JavaTokenParsers {
  var isParsing = false

  def boolean: Parser[Boolean] = ("#t" | "#f" | "true" | "false") ^^ { case "#t" => true; case "true" => true; case "#f" => false; case "false" => false }

  def int = wholeNumber ^^ { s => BigInt.apply(s) }

  def string: Parser[String] = stringLiteral ^^ { str => StringContext treatEscapes str.substring(1, str.length - 1) }

  def id = "([A-Za-z-!:?0-9])+".r ^^ {
    _.toString
  }

  def double = "[0-9]+[.][0-9]+".r ^^ {
    _.toDouble
  }

  var startNode: BigInt = BigInt.int2bigInt(-1)

  def list: Parser[List[Any]] = "(" ~> rep(expression) <~ ")" ^^ { s: List[Any] => s }

  var nodes: mutable.Map[NodeId, Node] = new mutable.HashMap[NodeId, Node]()
  var nodesX: mutable.Map[NodeId, Double] = new mutable.HashMap[NodeId, Double]()
  var nodesY: mutable.Map[NodeId, Double] = new mutable.HashMap[NodeId, Double]()
  var latestNode: Node = null
  var story: Story = null
  var isWithinNode = true

  def expression: Parser[Any] = list | boolean | double | int | id | string

  EventBus.StoryLoadedEvents foreach { n => {
    if (isParsing) {
      isParsing = false

      for ((k, v) <- nodes)
      {
        EventBus.send(MoveNode(v, nodesX.get(v.id).get, nodesY.get(v.id).get, "SchemeParser"))
      }
    }
  }
  }

  def parse(s: String) = {
    story = new Story()
    nodes.clear()
    nodesX.clear()
    nodesY.clear()
    isWithinNode = true
    latestNode = null
    startNode = BigInt.int2bigInt(-1)

    val list: List[Any] = this.parseAll(this.list, s).get
    process(list)
    isParsing = true

    for ((k, v) <- nodes) {
      story = story.addNode(v)
    }

    story
   // EventBus.send(NewStoryRequest("SchemeParser"))
  }

  def process(l: List[Any]): Unit = {
    val iterator = l.iterator
    while (iterator.hasNext) {
      val current = iterator.next()
      current match {
        case list: List[Any] => process(current.asInstanceOf[List[Any]])
        case currentString: String =>
          currentString match {
            case "begin" => processSection(iterator)
            case _ =>
          }
      }
    }
  }

  def processHeader(iterator: Iterator[Any]): Unit = {
    while (iterator.hasNext) {
      val current = iterator.next()
      current match {
        case list: List[Any] =>
          val i = current.asInstanceOf[List[Any]].iterator
          val firstToken = i.next()
          firstToken match {
            case instruction: String =>
              instruction match {
                case "create-node" =>
                  processCreateNode(i)
                  isWithinNode = true
                case "create-link" =>
                  processCreateLink(i)
                  isWithinNode = false
                case "begin" =>
                  processSubSection(i)
                case _ => //Console.println("Header: " + instruction)
              }
          }
        case _ =>
      }
    }
  }

  def processCreateTypedRule3(i: Iterator[Any]): Unit = {
    val ruleName = i.next().asInstanceOf[String]
    //val ruleType =
    i.next().asInstanceOf[List[Any]].lift(2)
    val stringOperator: String = i.next().asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
    val boolOperator = if (stringOperator == "or") BooleanOperator.Or else BooleanOperator.And
    //val negate =
    i.next().asInstanceOf[Boolean]
    val linkId = i.next().asInstanceOf[BigInt]
    //val stringFixedId =
    i.next().asInstanceOf[String]
    val fixedId = i.next().asInstanceOf[BigInt]
    //val stringFallthrough =
    i.next().asInstanceOf[String]
    val fallThrough = i.next().asInstanceOf[Boolean]

    val node = latestNode

    if (node != null) {
      val rule = new Rule(new RuleId(fixedId), ruleName, !fallThrough, boolOperator, List(), List())
      if (isWithinNode) {

        val newRules: List[Rule] = node.rules.:+(rule)
        val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

        nodes.remove(node.id)
        nodes.put(newNode.id, newNode)
        latestNode = newNode
      } else {
        node.content.rulesets.filter(_.id == new RulesetId(linkId)).foreach(ruleSet => {

          val newRules: List[Rule] = ruleSet.rules.:+(rule)

          val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
          val newFullSet: List[Ruleset] = node.content.rulesets.map(x => if (x == ruleSet) newSet else x)
          val newContent = node.content.copy(node.content.text, newFullSet)
          val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

          nodes.remove(node.id)
          nodes.put(newNode.id, newNode)
          latestNode = newNode
        })
      }
    }
  }

  def processFollowLink(expression: List[Any], parentRuleId: BigInt): Unit = {
    val newActionType = "LinkTo"
    //val linkId = expression.lift(2).get.asInstanceOf[BigInt]
    //val ruleId = expression.lift(3).get.asInstanceOf[BigInt]
    val toNodeId = expression.lift(5).get.asInstanceOf[BigInt]

    val node = latestNode
    if (node != null) {
      node.content.rulesets.foreach(ruleSet => {
        ruleSet.rules.foreach(rule => {
          if (rule.id == new RuleId(parentRuleId)) {
            val definitions = ActionDefinitions.apply()
            definitions.foreach(definition => {

              if (definition.actionType == new ActionType(newActionType)) {
                val params = Map(
                  new ParamName("node") -> ParamValue.Node.apply(new NodeId(toNodeId))
                )

                val newAction = new Action(new ActionType(newActionType), params)
                val newActions: List[Action] = rule.actions.:+(newAction)
                val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
                val newRules: List[Rule] = ruleSet.rules.map { x => if (x == rule) newRule else x }

                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(x => if (x == ruleSet) newSet else x)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                nodes.remove(node.id)
                nodes.put(newNode.id, newNode)
                latestNode = newNode
              }
            })
          }
        })
      })
    }
  }

  def processSetNumberFact(expression: List[Any], ruleId: BigInt): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val setOption = expression.lift(3).get.asInstanceOf[String] // Input, Random, Fact, Math

    var params = new mutable.ListMap[ParamName, ParamValue]()

    setOption match {
      case "Input" =>
        val setFactId = expression.lift(2).get.asInstanceOf[BigInt]
        //val setFactMode = expression.lift(3).get.asInstanceOf[String]
        val setFactValue = expression.lift(4).get.asInstanceOf[String]

        params += (new ParamName("updateValue") -> ParamValue.UnionValueSelected("inputValue"))
        params += (new ParamName("fact") -> ParamValue.IntegerFact(new FactId(setFactId)))
        params += (new ParamName("inputValue") -> ParamValue.IntegerInput(BigInt.apply(setFactValue)))
      case "Math" =>
        val optionExpression = expression.lift(4).get.asInstanceOf[List[Any]]
        val eOperator = optionExpression.lift(1).get.asInstanceOf[String]
        val value1 = optionExpression.lift(2).get.asInstanceOf[String] // FactId / Input
      val eOption1 = optionExpression.lift(3).get.asInstanceOf[String] // Fact, Input
      val value2 = optionExpression.lift(4).get.asInstanceOf[String] // FactId / Input
      val eOption2 = optionExpression.lift(5).get.asInstanceOf[String] // Fact, Input

        val operand1IsFact = if (eOption1 == "Fact") true else false
        val operand2IsFact = if (eOption2 == "Fact") true else false

        val operand1 = if (operand1IsFact) "factOperand1" else "userOperand1"
        val operand2 = if (operand2IsFact) "factOperand2" else "userOperand2"

        params += (new ParamName("fact") -> ParamValue.IntegerFact(new FactId(factId)))
        params += (new ParamName("updateValue") -> ParamValue.UnionValueSelected("computation"))
        params += (new ParamName("operand1") -> ParamValue.UnionValueSelected(operand1))
        if (operand1IsFact) {
          params += (new ParamName(operand1) -> ParamValue.IntegerFact(new FactId(BigInt.apply(value1))))
        } else {
          params += (new ParamName(operand1) -> ParamValue.IntegerInput(BigInt.apply(value1)))
        }
        params += (new ParamName("operator") -> ParamValue.SelectedListValue(eOperator))
        params += (new ParamName("operand2") -> ParamValue.UnionValueSelected(operand2))
        if (operand2IsFact) {
          params += (new ParamName(operand2) -> ParamValue.IntegerFact(new FactId(BigInt.apply(value2))))
        } else {
          params += (new ParamName(operand2) -> ParamValue.IntegerInput(BigInt.apply(value2)))
        }
        params += (new ParamName("computation") -> ParamValue.ProductValue(List("operand1", "operator", "operand2")))
      case "Fact" =>
        val setFromFactId = expression.lift(2).get.asInstanceOf[BigInt]
        val setToFactId = expression.lift(4).get.asInstanceOf[BigInt]
        params += (new ParamName("updateValue") -> ParamValue.UnionValueSelected("integerFactValue"))
        params += (new ParamName("fact") -> ParamValue.IntegerFact(new FactId(setFromFactId)))
        params += (new ParamName("integerFactValue") -> ParamValue.IntegerFact(new FactId(setToFactId)))

      case "Random" =>
        val optionExpression = expression.lift(4).get.asInstanceOf[List[Any]]
        val value1 = optionExpression.lift(1).get.asInstanceOf[String] // FactId / Input
      val eOption1 = optionExpression.lift(2).get.asInstanceOf[String] // Fact, Input
      val value2 = optionExpression.lift(3).get.asInstanceOf[String] // FactId / Input
      val eOption2 = optionExpression.lift(4).get.asInstanceOf[String] // Fact, Input

        val operand1IsFact = if (eOption1 == "Fact") true else false
        val operand2IsFact = if (eOption2 == "Fact") true else false

        val operand1 = if (operand1IsFact) "startFact" else "startInput"
        val operand2 = if (operand2IsFact) "endFact" else "endInput"

        params += (new ParamName("randomValue") -> ParamValue.ProductValue(List("start", "end")))
        params += (new ParamName("updateValue") -> ParamValue.UnionValueSelected("randomValue"))
        params += (new ParamName("fact") -> ParamValue.IntegerFact(new FactId(factId)))

        params += (new ParamName("start") -> ParamValue.UnionValueSelected(operand1))
        if (operand1IsFact) {
          params += (new ParamName(operand1) -> ParamValue.IntegerFact(new FactId(BigInt.apply(value1))))
        } else {
          params += (new ParamName(operand1) -> ParamValue.IntegerInput(BigInt.apply(value1)))
        }

        params += (new ParamName("end") -> ParamValue.UnionValueSelected(operand2))
        if (operand2IsFact) {
          params += (new ParamName(operand2) -> ParamValue.IntegerFact(new FactId(BigInt.apply(value2))))
        } else {
          params += (new ParamName(operand2) -> ParamValue.IntegerInput(BigInt.apply(value2)))
        }
    }

    val action = new Action(new ActionType("UpdateIntegerFacts"), params.toMap)

    action
  }

  def processActionSetFact(expression: List[Any], parentRuleId: BigInt, boolValue: Boolean): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val params = Map(
      new ParamName("fact") -> new ParamValue.BooleanFact(new FactId(factId)),
      new ParamName("value") -> new SelectedListValue(if (boolValue) "true" else "false")
    )

    val action = new Action(new ActionType("UpdateBooleanFact"), params)

    action
  }

  def processAnywhereCheck(expression: List[Any], parentRuleId: BigInt): Action = {
    //val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val action = new Action(new ActionType("EnableAnywhereLinkToHere"), Map())
    action
  }

  def processDisplayedNode(expression: List[Any], parentRuleId: BigInt): Action = {
    //val instruction = expression.lift(1).get.asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]

    val factType = expression.lift(2).get.asInstanceOf[String]
    val value = expression.lift(3).get
    //val actionId = expression.lift(4).get.asInstanceOf[BigInt]

    var params: Map[ParamName, ParamValue] = null

    factType match {
      case "alternative text" =>
        params = Map(
          new ParamName("textInput") -> new StringInput(value.asInstanceOf[String]),
          new ParamName("text") -> new UnionValueSelected("textInput")
        )
      case "text fact" =>
        params = Map(
          new ParamName("stringFactValue") -> new ParamValue.StringFact(new FactId(value.asInstanceOf[BigInt])),
          new ParamName("text") -> new UnionValueSelected("stringFactValue")
        )
      case "number fact" =>
        params = Map(
          new ParamName("NumberFactValue") -> new ParamValue.IntegerFact(new FactId(value.asInstanceOf[BigInt])),
          new ParamName("text") -> new UnionValueSelected("NumberFactValue")
        )
    }

    new Action(new ActionType("UpdateText"), params)
  }

  def processSetValue(expression: List[Any], parentRuleId: BigInt): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val value = expression.lift(3).get.asInstanceOf[String]

    val params = Map(
      new ParamName("fact") -> new ParamValue.StringFact(new FactId(factId)),
      new ParamName("value") -> new StringInput(value)
    )

    new Action(new ActionType("UpdateStringFact"), params)
  }

  def processCreateAction(i: Iterator[Any]): Unit = {
    //val actionName =
    i.next().asInstanceOf[String]
    val actionType = i.next().asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
    val expression = i.next().asInstanceOf[List[Any]]
    val parentRuleId = i.next().asInstanceOf[BigInt]
    //val actionId =
    i.next().asInstanceOf[BigInt]
    //var newActionType = actionType

    actionType match {
      case "clicked-link" =>
        val linkActionType = expression.lift(1).get.asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]

        var action: Action = null

        linkActionType match {
          case "follow-link" => processFollowLink(expression, parentRuleId)
          case "assert" => val boolValue = true; action = processActionSetFact(expression, parentRuleId, boolValue)
          case "retract" => val boolValue = false; action = processActionSetFact(expression, parentRuleId, boolValue)
          case "set-number-fact" => action = processSetNumberFact(expression, parentRuleId)
          case "anywhere-check" => action = processAnywhereCheck(expression, parentRuleId)
          case "set-value!" => action = processSetValue(expression, parentRuleId)
          case _ => //Console.println("Link Action: " + linkActionType + " " + expression)
        }

        if (action != null) {
          val node = latestNode

          if (node != null) {
            node.content.rulesets.foreach(ruleSet => {
              ruleSet.rules.filter(_.id == new RuleId(parentRuleId)).foreach(rule => {

                val newActions = rule.actions.:+(action)
                val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
                val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }
                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                nodes.remove(node.id)
                nodes.put(newNode.id, newNode)
                latestNode = newNode
              })
            })
          }
        }
      case "entered-node" =>
        val linkActionType = expression.lift(1).get.asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
        var action: Action = null
        linkActionType match {
          case "retract" => val boolValue = false; action = processActionSetFact(expression, parentRuleId, boolValue)
          case "assert" => val boolValue = true; action = processActionSetFact(expression, parentRuleId, boolValue)
          case "set-number-fact" => action = processSetNumberFact(expression, parentRuleId)
          case "anywhere-check" => action = processAnywhereCheck(expression, parentRuleId)
          case "set-value!" => action = processSetValue(expression, parentRuleId)
          case _ => //Console.println("Link Action: " + linkActionType + " " + expression)
        }

        if (action != null) {
          val node = latestNode

          if (node != null) {
            node.rules.filter(_.id == new RuleId(parentRuleId)).foreach(rule => {
              val newActions = rule.actions.:+(action)
              val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
              val newRules: List[Rule] = node.rules.map { i => if (i == rule) newRule else i }
              val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

              nodes.remove(node.id)
              nodes.put(newNode.id, newNode)
              latestNode = newNode
            })
          }
        }
      case "anywhere-check" =>
        val action = processAnywhereCheck(expression, parentRuleId)

        if (action != null) {
          val node = latestNode

          if (node != null) {
            node.rules.filter(_.id == new RuleId(parentRuleId)).foreach(rule => {
              val newActions = rule.actions.:+(action)
              val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
              val newRules: List[Rule] = node.rules.map { i => if (i == rule) newRule else i }
              val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

              nodes.remove(node.id)
              nodes.put(newNode.id, newNode)
              latestNode = newNode
            })
          }
        }
      case "displayed-node" =>
        val action = processDisplayedNode(expression, parentRuleId)

        if (action != null) {
          val node = latestNode

          if (node != null) {
            node.content.rulesets.foreach(ruleSet => {
              ruleSet.rules.filter(_.id == new RuleId(parentRuleId)).foreach(rule => {

                val newActions = rule.actions.:+(action)
                val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
                val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }
                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                nodes.remove(node.id)
                nodes.put(newNode.id, newNode)
                latestNode = newNode
              })
            })
          }
        }
      case _ => //Console.println("Action: " + actionType + " " + expression)
    }

  }

  def processCreateTypedCondition2(i: Iterator[Any]): Unit = {
    //val conditionName =
    i.next().asInstanceOf[String]
    val conditionType = i.next().asInstanceOf[BigInt]

    var newConditionType = ""

    conditionType.toInt match {
      case 0 => newConditionType = "NodeCondition"
      case 1 => newConditionType = "LinkCondition"
      case 2 => newConditionType = "BooleanFactValue"
      case 3 => newConditionType = "IntegerFactComparison"
      case _ => //Console.println("Condition Type: " + conditionType)
    }

    val targetId = i.next().asInstanceOf[BigInt] // Node/Link/Fact ID

    var status = i.next()

    val ruleId = i.next().asInstanceOf[BigInt]
    //val fixedIdString =
    i.next().asInstanceOf[String]
    //val conditionId =
    i.next().asInstanceOf[BigInt]
    //val numFactArgsString =
    i.next().asInstanceOf[String]
    val numFactArgs = i.next()

    val node = latestNode
    if (node != null) {
      if (isWithinNode) {
        node.rules.foreach(rule => {
          if (rule.id == new RuleId(ruleId)) {
            val definitions = ConditionDefinitions.apply()
            definitions.foreach(definition => {
              if (definition.conditionType == new ConditionType(newConditionType)) {
                var params: Map[ParamName, ParamValue] = Map()
                newConditionType match {
                  case "NodeCondition" =>
                    params = Map(
                      new ParamName("node") -> new ParamValue.Node(new NodeId(targetId)),
                      new ParamName("status") -> new SelectedListValue(status.asInstanceOf[BigInt].toInt match { case 0 => "not visited"; case 1 => "visited"; case 2 => "is previous"; case 3 => "is not previous"; case 4 => "current" })
                    )

                  case "LinkCondition" =>
                    params = Map(
                      new ParamName("link") -> new ParamValue.Link(new RulesetId(targetId)),
                      new ParamName("status") -> new SelectedListValue(if (status.asInstanceOf[BigInt] == BigInt.apply(1)) "followed" else "not followed")
                    )

                  case "BooleanFactValue" =>

                    if (status.isInstanceOf[BigInt]) {
                      status = if (status == BigInt.apply(1)) true else false
                    }
                    params = Map(
                      new ParamName("fact") -> new ParamValue.BooleanFact(new FactId(targetId)),
                      new ParamName("state") -> new SelectedListValue(if (status.asInstanceOf[Boolean]) "true" else "false")
                    )

                  case "IntegerFactComparison" =>
                    val numFactArguments = numFactArgs.asInstanceOf[List[Any]].iterator
                    //val listString =
                    numFactArguments.next().asInstanceOf[String]
                    val operator = numFactArguments.next().asInstanceOf[String]
                    val mode = numFactArguments.next().asInstanceOf[String]
                    val value = numFactArguments.next().asInstanceOf[String]
                    val paramV = if (mode == "Fact") new ParamValue.IntegerFact(new FactId(BigInt.apply(value))) else new ParamValue.IntegerInput(BigInt.apply(value))
                    params = Map(
                      new ParamName("fact") -> new ParamValue.IntegerFact(new FactId(targetId)),
                      new ParamName(if (mode == "Fact") "otherFact" else "input") -> paramV,
                      new ParamName("operator") -> new SelectedListValue(operator),
                      new ParamName("comparisonValue") -> new UnionValueSelected(if (mode == "Fact") "otherFact" else "input")
                    )

                  case _ =>
                }

                val newCondition = new Condition(new ConditionType(newConditionType), params)
                val newConditions = newCondition :: rule.conditions
                val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, newConditions, rule.actions)
                val newRules = node.rules.map { i => if (i == rule) newRule else i }

                val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

                nodes.remove(node.id)
                nodes.put(newNode.id, newNode)
                latestNode = newNode
              }
            })
          }
        })
      } else {
        node.content.rulesets.foreach(ruleSet => {
          ruleSet.rules.foreach(rule => {
            if (rule.id == new RuleId(ruleId)) {
              val definitions = ConditionDefinitions.apply()

              definitions.foreach(definition => {
                if (definition.conditionType == new ConditionType(newConditionType)) {
                  var params: Map[ParamName, ParamValue] = Map()
                  newConditionType match {
                    case "NodeCondition" =>
                      params = Map(
                        new ParamName("node") -> new ParamValue.Node(new NodeId(targetId)),
                        new ParamName("status") -> new SelectedListValue(status.asInstanceOf[BigInt].toInt match { case 0 => "not visited"; case 1 => "visited"; case 2 => "is previous"; case 3 => "is not previous"; case 4 => "current" })
                      )

                    case "LinkCondition" =>
                      params = Map(
                        new ParamName("link") -> new ParamValue.Link(new RulesetId(targetId)),
                        new ParamName("status") -> new SelectedListValue(if (status.asInstanceOf[BigInt] == BigInt.apply(1)) "followed" else "not followed")
                      )

                    case "BooleanFactValue" =>

                      if (status.isInstanceOf[BigInt]) {
                        status = if (status == BigInt.apply(1)) true else false
                      }

                      params = Map(
                        new ParamName("fact") -> new ParamValue.BooleanFact(new FactId(targetId)),
                        new ParamName("state") -> new SelectedListValue(if (status.asInstanceOf[Boolean]) "true" else "false")
                      )

                    case "IntegerFactComparison" =>
                      val numFactArguments = numFactArgs.asInstanceOf[List[Any]].iterator
                      //val listString =
                      numFactArguments.next().asInstanceOf[String]
                      val operator = numFactArguments.next().asInstanceOf[String]
                      val mode = numFactArguments.next().asInstanceOf[String]
                      val value = numFactArguments.next().asInstanceOf[String]
                      val paramV = if (mode == "Fact") new ParamValue.IntegerFact(new FactId(BigInt.apply(value))) else new ParamValue.IntegerInput(BigInt.apply(value))
                      params = Map(
                        new ParamName("fact") -> new ParamValue.IntegerFact(new FactId(targetId)),
                        new ParamName(if (mode == "Fact") "otherFact" else "input") -> paramV,
                        new ParamName("operator") -> new SelectedListValue(operator),
                        new ParamName("comparisonValue") -> new UnionValueSelected(if (mode == "Fact") "otherFact" else "input")
                      )

                    case _ =>
                  }

                  val newCondition = new Condition(new ConditionType(newConditionType), params)
                  val newConditions: List[Condition] = newCondition :: rule.conditions
                  val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, newConditions, rule.actions)
                  val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }

                  val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                  val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                  val newContent = node.content.copy(node.content.text, newFullSet)
                  val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                  nodes.remove(node.id)
                  nodes.put(newNode.id, newNode)
                  latestNode = newNode
                }
              })
            }
          })
        })
      }
    }
  }

  def processSubSection(iterator: Iterator[Any]): Unit = {
    while (iterator.hasNext) {
      val current = iterator.next()
      current match {
        case list: List[Any] =>
          val i = current.asInstanceOf[List[Any]].iterator
          val firstToken = i.next()
          firstToken match {
            case str: String =>
              val instruction = firstToken.asInstanceOf[String]
              instruction match {
                case "create-typed-rule3" => processCreateTypedRule3(i)
                case "create-action" => processCreateAction(i)
                case "create-typed-condition2" => processCreateTypedCondition2(i)
                case _ =>
              }
          }
      }
    }
  }

  def processCreateLink(i: Iterator[Any]): Unit = {
    val linkName = i.next().asInstanceOf[String]
    val fromNodeID = i.next().asInstanceOf[BigInt]
    //val toNodeID =
    i.next().asInstanceOf[BigInt]
    val startIndex = i.next().asInstanceOf[BigInt]
    val endIndex = i.next().asInstanceOf[BigInt]
    //val useDestination =
    i.next().asInstanceOf[Boolean]
    //val useAltDestination =
    i.next().asInstanceOf[Boolean]
    //val useAltText =
    i.next().asInstanceOf[Boolean]
    //val altDestination =
    i.next().asInstanceOf[BigInt]
    //val altText =
    i.next().asInstanceOf[String]
    //val updateDisplay =
    i.next().asInstanceOf[Boolean]
    val linkId = i.next().asInstanceOf[BigInt]

    val node = nodes.get(new NodeId(fromNodeID)).get

    if (node != null) {
      val newSet = new Ruleset(new RulesetId(linkId), linkName, new RulesetIndexes(new TextIndex(startIndex), new TextIndex(endIndex)), List())
      val newContent = node.content.copy(node.content.text, node.content.rulesets.:+(newSet))
      val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

      nodes.remove(node.id)
      nodes.put(newNode.id, newNode)

      latestNode = newNode
    }
  }

  def processCreateNode(i: Iterator[Any]): Unit = {
    val nameValue = i.next().asInstanceOf[String]
    val contentText = i.next().asInstanceOf[String]
    var next = i.next()
    val x = next match {
      case bigInt: BigInt => bigInt.doubleValue;
      case _ => next.asInstanceOf[Double]
    }
    next = i.next()
    val y = next match {
      case bigInt: BigInt => bigInt.doubleValue;
      case _ => next.asInstanceOf[Double]
    }
    //val isAnywhere =
    i.next().asInstanceOf[Boolean]
    //val updateDisplay =
    i.next()
    val nodeId = i.next().asInstanceOf[BigInt]

    val ruleSet: List[Rule] = List()

    val node = new Node(new NodeId(nodeId), nameValue, new NodeContent(contentText, List()), if (nodeId == startNode) true else false, ruleSet)

    nodes.put(node.id, node)
    nodesX.put(node.id, x)
    nodesY.put(node.id, y)

    latestNode = node
  }

  def processCreateFact(i: Iterator[Any]) = {
    while (i.hasNext) {
      val factName = i.next().asInstanceOf[String]
      val factType = i.next().asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
      val factId = new FactId(i.next().asInstanceOf[BigInt])

      var fact: Fact = null

      factType match {
        case "boolean" => val boolValue = false; fact = BooleanFact.apply(factId, factName, boolValue)
        case "string" => fact = StringFact.apply(factId, factName, "")
        case "number" => fact = IntegerFact.apply(factId, factName, 0)
      }

      story = story.addFact(fact)
    }
  }

  def processSection(iterator: Iterator[Any]): Unit = {
    val headers = new scala.collection.mutable.Queue[List[Any]]
    while (iterator.hasNext) {
      val current = iterator.next()
      current match {
        case list: List[Any] =>
          val i = current.asInstanceOf[List[Any]].iterator
          val firstToken = i.next()
          firstToken match {
            case instruction: String =>
              instruction match {
                case "make-hypertext" => processHeader(i)
                case "set-story-title!" => story = story.copy(i.next().asInstanceOf[String])
                case "set-author-name!" => story = story.changeAuthor(i.next().asInstanceOf[String])
                case "set-story-comment!" => story = story.updateMetadata(story.metadata.copy(i.next().asInstanceOf[String]))
                case "set-disable-back-button!" => story = story.updateMetadata(story.metadata.copy(story.metadata.comments, story.metadata.readerStyle, i.next().asInstanceOf[Boolean]))
                case "set-disable-restart-button!" => story = story.updateMetadata(story.metadata.copy(story.metadata.comments, story.metadata.readerStyle, story.metadata.isBackButtonDisabled, i.next().asInstanceOf[Boolean]))
                case "set-start-node!" => startNode = i.next().asInstanceOf[BigInt]
                case "create-fact" => processCreateFact(i)
                case "begin" => headers.enqueue(current.asInstanceOf[List[Any]])
                case _ => //Console.println("Base: " + instruction)
              }
          }
      }
    }

    while (headers.nonEmpty) {
      val i = headers.dequeue().iterator
      processHeader(i)
    }
  }
}