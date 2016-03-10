package org.narrativeandplay.hypedyn.utils.parsing

import scala.collection.mutable

import fastparse.all._

import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetId, RulesetIndexes, TextIndex}
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.story.internal.NodeContent.Ruleset
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamValue.{SelectedListValue, StringInput, UnionValueSelected}
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, _}
import org.narrativeandplay.hypedyn.story.rules.{BooleanOperator, _}

object SchemeParser {

  /**
   * Keeps track of node positions
   */
  private var nodePositions = AstList()

  /**
   * Parses a string into a Story
   *
   * @param s The input string to parse
   * @return The parsed story
   */
  def parse(s: String): Map[String, Any] = {

    val spaceValue = P(CharsWhile(" \r\n".contains(_: Char)).?)
    val digitValue = P("-".? ~ CharsWhile('0' to '9' contains (_: Char)).!).map(x => if (x.nonEmpty) BigInt(x.toString))
    val doubleValue = P((digitValue ~ "." ~ digitValue).!).map(x => x.toString.toDouble)

    val trueValue = P(P("#t") | P("true")).map(_ => true)
    val falseValue = P(P("#f") | P("false")).map(_ => false)
    val booleanValue = trueValue | falseValue

    val escape = P(P("\\") ~ CharIn("\"/\\bfnrt").!)

    val stringCharsValue = P(CharsWhile(!"\"\\".contains(_: Char)).!)
    val identifier = P(CharsWhile(!"\" )".contains(_: Char)).!)

    val stringLiteralValue = P(spaceValue ~
      "\"" ~ (stringCharsValue | escape).rep.! ~ "\"").map(StringContext treatEscapes _.toString)

    lazy val listValue = P("(" ~ expressionValue.rep.? ~ ")").map(x => if (x.nonEmpty) x.get.toList)
    lazy val expressionValue: P[Any] = P(spaceValue ~
      P(listValue | booleanValue | doubleValue | digitValue | identifier | stringLiteralValue) ~ spaceValue)

    var story = Story()

    val list: List[Any] = expressionValue.parse(s).get.value.asInstanceOf[List[Any]]

    story = process(list, story)

    val mapFields: AstMap = AstMap("zoomLevel" -> AstFloat(1.0), "nodes" -> nodePositions)
    val pluginData: AstElement = AstMap("Default Story Viewer" -> mapFields)

    val map = Map("story" -> story, "plugins" -> pluginData)

    map
  }

  /**
   * Processes a list of tokens into a Story
   *
   * @param l     The list of tokens to be parsed
   * @param story The existing story to parse the tokens into
   * @return The updated story
   */
  private def process(l: List[Any], story: Story): Story = {
    var newStory = story

    val iterator = l.iterator
    while (iterator.hasNext) {
      val current = iterator.next()
      current match {
        case list: List[Any] => newStory = process(current.asInstanceOf[List[Any]], newStory)
        case currentString: String =>
          currentString match {
            case "begin" => newStory = processFirstLevel(iterator, newStory)
            case _ =>
          }
      }
    }

    newStory
  }

  /**
   * Processes the header tokens
   *
   * @param iterator The token iterator
   * @param story    The story to be updated
   * @return The updated story
   */
  private def processHeader(iterator: Iterator[Any], story: Story): Story = {
    var newStory = story
    var currentNode = Option.empty[Node]
    var isWithinNode = true

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
                  val nodeOption = Option(processCreateNode(i))
                  if (nodeOption.nonEmpty) newStory = newStory.addNode(nodeOption.get)
                  currentNode = nodeOption
                  isWithinNode = true
                case "create-link" =>
                  val nodeOption = processCreateLink(i, newStory)

                  if (nodeOption.nonEmpty) {
                    newStory.nodes.filter(_.id == nodeOption.get.id).foreach(node => {
                      newStory = newStory.removeNode(node)
                    })
                    newStory = newStory.addNode(nodeOption.get)
                  }
                  currentNode = nodeOption
                  isWithinNode = false
                case "begin" =>
                  val nodeOption = processSecondLevel(i, currentNode, isWithinNode)
                  if (nodeOption.nonEmpty) {
                    newStory.nodes.filter(_.id == nodeOption.get.id).foreach(node => {
                      newStory = newStory.removeNode(node)
                    })
                    newStory = newStory.addNode(nodeOption.get)
                  }
                  currentNode = nodeOption
                case _ => //Console.println("Header: " + instruction)
              }
          }
        case _ =>
      }
    }

    newStory
  }

  /**
   * Process the tokens for create-typed-rule-3 rule
   *
   * @param i            The token iterator
   * @param currentNode  The latest node processed
   * @param isWithinNode Flag that determines if the rule resides at the Node or Fragment level
   * @return The modified node
   */
  private def processCreateTypedRule3(i: Iterator[Any],
                                      currentNode: Option[Node],
                                      isWithinNode: Boolean): Option[Node] = {

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

    val rule = Rule(RuleId(fixedId), ruleName, !fallThrough, boolOperator, List(), List())
    if (currentNode.nonEmpty) {
      val node = currentNode.get
      if (isWithinNode) {

        val newRules: List[Rule] = node.rules.:+(rule)
        val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

        return Option(newNode)
      } else {
        node.content.rulesets.filter(_.id == RulesetId(linkId)).foreach(ruleSet => {

          val newRules: List[Rule] = ruleSet.rules.:+(rule)

          val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
          val newFullSet: List[Ruleset] = node.content.rulesets.map(x => if (x == ruleSet) newSet else x)
          val newContent = node.content.copy(node.content.text, newFullSet)
          val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

          return Option(newNode)
        })
      }
    }

    Option.empty[Node]
  }

  /**
   * Process the tokens for the follow-link rule
   *
   * @param expression   The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @param currentNode  The latest node processed
   * @return
   */
  private def processFollowLink(expression: List[Any],
                                parentRuleId: BigInt,
                                currentNode: Option[Node]): Option[Node] = {
    val newActionType = "LinkTo"
    //val linkId = expression.lift(2).get.asInstanceOf[BigInt]
    //val ruleId = expression.lift(3).get.asInstanceOf[BigInt]
    val toNodeId = expression.lift(5).get.asInstanceOf[BigInt]


    if (currentNode.nonEmpty) {
      val node = currentNode.get
      node.content.rulesets.foreach(ruleSet => {
        ruleSet.rules.foreach(rule => {
          if (rule.id == RuleId(parentRuleId)) {
            val definitions = ActionDefinitions()
            definitions.foreach(definition => {

              if (definition.actionType == ActionType(newActionType)) {
                val params = Map(
                  ParamName("node") -> ParamValue.Node(NodeId(toNodeId))
                )

                val newAction = Action(ActionType(newActionType), params)
                val newActions: List[Action] = rule.actions.:+(newAction)
                val newRule = rule.copy(
                  rule.id,
                  rule.name,
                  rule.stopIfTrue,
                  rule.conditionsOp,
                  rule.conditions,
                  newActions
                )
                val newRules: List[Rule] = ruleSet.rules.map { x => if (x == rule) newRule else x }

                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(x => if (x == ruleSet) newSet else x)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                return Option(newNode)
              }
            })
          }
        })
      })
    }

    Option.empty[Node]
  }

  /**
   * Processes the tokens for the set-number-fact action
   *
   * @param expression The combination of tokens that make up an expression
   * @param ruleId     The rule identifier
   * @return The created action
   */
  private def processSetNumberFact(expression: List[Any], ruleId: BigInt): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val setOption = expression.lift(3).get.asInstanceOf[String] // Input, Random, Fact, Math

    val params = new mutable.ListMap[ParamName, ParamValue]()

    setOption match {
      case "Input" =>
        val setFactId = expression.lift(2).get.asInstanceOf[BigInt]
        //val setFactMode = expression.lift(3).get.asInstanceOf[String]
        val setFactValue = expression.lift(4).get.asInstanceOf[String]

        params += (ParamName("updateValue") -> ParamValue.UnionValueSelected("inputValue"))
        params += (ParamName("fact") -> ParamValue.IntegerFact(FactId(setFactId)))
        params += (ParamName("inputValue") -> ParamValue.IntegerInput(BigInt(setFactValue)))
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

        params += (ParamName("fact") -> ParamValue.IntegerFact(FactId(factId)))
        params += (ParamName("updateValue") -> ParamValue.UnionValueSelected("computation"))
        params += (ParamName("operand1") -> ParamValue.UnionValueSelected(operand1))
        if (operand1IsFact) {
          params += (ParamName(operand1) -> ParamValue.IntegerFact(FactId(BigInt(value1))))
        } else {
          params += (ParamName(operand1) -> ParamValue.IntegerInput(BigInt(value1)))
        }
        params += (ParamName("operator") -> ParamValue.SelectedListValue(eOperator))
        params += (ParamName("operand2") -> ParamValue.UnionValueSelected(operand2))
        if (operand2IsFact) {
          params += (ParamName(operand2) -> ParamValue.IntegerFact(FactId(BigInt(value2))))
        } else {
          params += (ParamName(operand2) -> ParamValue.IntegerInput(BigInt(value2)))
        }
        params += (ParamName("computation") -> ParamValue.ProductValue(List("operand1", "operator", "operand2")))
      case "Fact" =>
        val setFromFactId = expression.lift(2).get.asInstanceOf[BigInt]
        val setToFactId = expression.lift(4).get.asInstanceOf[BigInt]
        params += (ParamName("updateValue") -> ParamValue.UnionValueSelected("integerFactValue"))
        params += (ParamName("fact") -> ParamValue.IntegerFact(FactId(setFromFactId)))
        params += (ParamName("integerFactValue") -> ParamValue.IntegerFact(FactId(setToFactId)))

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

        params += (ParamName("randomValue") -> ParamValue.ProductValue(List("start", "end")))
        params += (ParamName("updateValue") -> ParamValue.UnionValueSelected("randomValue"))
        params += (ParamName("fact") -> ParamValue.IntegerFact(FactId(factId)))

        params += (ParamName("start") -> ParamValue.UnionValueSelected(operand1))
        if (operand1IsFact) {
          params += (ParamName(operand1) -> ParamValue.IntegerFact(FactId(BigInt(value1))))
        } else {
          params += (ParamName(operand1) -> ParamValue.IntegerInput(BigInt(value1)))
        }

        params += (ParamName("end") -> ParamValue.UnionValueSelected(operand2))
        if (operand2IsFact) {
          params += (ParamName(operand2) -> ParamValue.IntegerFact(FactId(BigInt(value2))))
        } else {
          params += (ParamName(operand2) -> ParamValue.IntegerInput(BigInt(value2)))
        }
    }

    val action = Action(ActionType("UpdateIntegerFacts"), params.toMap)

    action
  }

  /**
   * Processes the tokens that set a boolean fact
   *
   * @param expression   The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @param boolValue    The value to set the boolean fact to
   * @return The created action
   */
  private def processActionSetFact(expression: List[Any], parentRuleId: BigInt, boolValue: Boolean): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val params = Map(
      ParamName("fact") -> ParamValue.BooleanFact(FactId(factId)),
      ParamName("value") -> SelectedListValue(if (boolValue) "true" else "false")
    )

    val action = Action(ActionType("UpdateBooleanFact"), params)

    action
  }

  /**
   * Processes the tokens that enables anywhere links to a node
   *
   * @param expression   The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processAnywhereCheck(expression: List[Any], parentRuleId: BigInt): Action = {
    //val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val action = Action(ActionType("EnableAnywhereLinkToHere"), Map())
    action
  }

  /**
   * Processes the tokens for the displayed-node action
   *
   * @param expression   The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processDisplayedNode(expression: List[Any], parentRuleId: BigInt): Action = {
    //val instruction = expression.lift(1).get.asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]

    val factType = expression.lift(2).get.asInstanceOf[String]
    val value = expression.lift(3).get
    //val actionId = expression.lift(4).get.asInstanceOf[BigInt]

    var params: Map[ParamName, ParamValue] = Map()

    factType match {
      case "alternative text" =>
        params = Map(
          ParamName("textInput") -> StringInput(value.asInstanceOf[String]),
          ParamName("text") -> UnionValueSelected("textInput")
        )
      case "text fact" =>
        params = Map(
          ParamName("stringFactValue") -> ParamValue.StringFact(FactId(value.asInstanceOf[BigInt])),
          ParamName("text") -> UnionValueSelected("stringFactValue")
        )
      case "number fact" =>
        params = Map(
          ParamName("NumberFactValue") -> ParamValue.IntegerFact(FactId(value.asInstanceOf[BigInt])),
          ParamName("text") -> UnionValueSelected("NumberFactValue")
        )
    }

    Action(ActionType("UpdateText"), params)
  }

  /**
   * Processes the tokens for the set-value! action
   *
   * @param expression   The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processSetValue(expression: List[Any], parentRuleId: BigInt): Action = {
    val factId = expression.lift(2).get.asInstanceOf[BigInt]
    val value = expression.lift(3).get.asInstanceOf[String]

    val params = Map(
      ParamName("fact") -> ParamValue.StringFact(FactId(factId)),
      ParamName("value") -> StringInput(value)
    )

    Action(ActionType("UpdateStringFact"), params)
  }

  /**
   * Processes the tokens for the create-action rule
   *
   * @param i           The token iterator
   * @param currentNode The latest node processed
   * @return The modified node
   */
  private def processCreateAction(i: Iterator[Any], currentNode: Option[Node]): Option[Node] = {
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

        var actionOption = Option.empty[Action]

        linkActionType match {
          case "follow-link" =>
            return processFollowLink(expression, parentRuleId, currentNode)
          case "assert" =>
            val boolValue = true
            actionOption = Option(processActionSetFact(expression, parentRuleId, boolValue))
          case "retract" =>
            val boolValue = false
            actionOption = Option(processActionSetFact(expression, parentRuleId, boolValue))
          case "set-number-fact" => actionOption = Option(processSetNumberFact(expression, parentRuleId))
          case "anywhere-check" => actionOption = Option(processAnywhereCheck(expression, parentRuleId))
          case "set-value!" => actionOption = Option(processSetValue(expression, parentRuleId))
          case _ => //Console.println("Link Action: " + linkActionType + " " + expression)
        }

        if (actionOption.nonEmpty) {
          val action = actionOption.get

          if (currentNode.nonEmpty) {
            val node = currentNode.get
            node.content.rulesets.foreach(ruleSet => {
              ruleSet.rules.filter(_.id == RuleId(parentRuleId)).foreach(rule => {

                val newActions = rule.actions.:+(action)
                val newRule = rule.copy(
                  rule.id,
                  rule.name,
                  rule.stopIfTrue,
                  rule.conditionsOp,
                  rule.conditions, newActions
                )
                val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }
                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                return Option(newNode)
              })
            })
          }
        }
      case "entered-node" =>
        val linkActionType = expression.lift(1).get.asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
        var action = Option.empty[Action]
        linkActionType match {
          case "retract" =>
            val boolValue = false
            action = Option(processActionSetFact(expression, parentRuleId, boolValue))
          case "assert" =>
            val boolValue = true
            action = Option(processActionSetFact(expression, parentRuleId, boolValue))
          case "set-number-fact" => action = Option(processSetNumberFact(expression, parentRuleId))
          case "anywhere-check" => action = Option(processAnywhereCheck(expression, parentRuleId))
          case "set-value!" => action = Option(processSetValue(expression, parentRuleId))
          case _ => //Console.println("Link Action: " + linkActionType + " " + expression)
        }

        if (action.nonEmpty) {
          if (currentNode.nonEmpty) {
            val node = currentNode.get
            node.rules.filter(_.id == RuleId(parentRuleId)).foreach(rule => {
              val newActions = rule.actions.:+(action.get)
              val newRule = rule.copy(
                rule.id,
                rule.name,
                rule.stopIfTrue,
                rule.conditionsOp,
                rule.conditions,
                newActions
              )
              val newRules: List[Rule] = node.rules.map { i => if (i == rule) newRule else i }
              val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

              return Option(newNode)
            })
          }
        }
      case "anywhere-check" =>
        val action = processAnywhereCheck(expression, parentRuleId)

        if (currentNode.nonEmpty) {
          val node = currentNode.get
          node.rules.filter(_.id == RuleId(parentRuleId)).foreach(rule => {
            val newActions = rule.actions.:+(action)
            val newRule = rule.copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, rule.conditions, newActions)
            val newRules: List[Rule] = node.rules.map { i => if (i == rule) newRule else i }
            val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

            return Option(newNode)
          })
        }
      case "displayed-node" =>
        val action = processDisplayedNode(expression, parentRuleId)

        if (currentNode.nonEmpty) {
          val node = currentNode.get
          node.content.rulesets.foreach(ruleSet => {
            ruleSet.rules.filter(_.id == RuleId(parentRuleId)).foreach(rule => {

              val newActions = rule.actions.:+(action)
              val newRule = rule.copy(
                rule.id,
                rule.name,
                rule.stopIfTrue,
                rule.conditionsOp,
                rule.conditions,
                newActions
              )
              val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }
              val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
              val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
              val newContent = node.content.copy(node.content.text, newFullSet)
              val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

              return Option(newNode)
            })
          })
        }
      case _ =>
    }

    Option.empty[Node]
  }

  /**
   * Process the tokens for create-typed-condition-2 rule
   *
   * @param i            The token iterator
   * @param currentNode  The latest node processed
   * @param isWithinNode Flag that determines if the rule resides at the Node or Fragment level
   * @return The modified node
   */
  private def processCreateTypedCondition2(i: Iterator[Any],
                                           currentNode: Option[Node],
                                           isWithinNode: Boolean): Option[Node] = {
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


    if (currentNode.nonEmpty) {
      val node = currentNode.get

      if (isWithinNode) {
        node.rules.filter(_.id == RuleId(ruleId)).foreach(rule => {
          val definitions = ConditionDefinitions()
          definitions.foreach(definition => {
            if (definition.conditionType == ConditionType(newConditionType)) {
              var params: Map[ParamName, ParamValue] = Map()
              newConditionType match {
                case "NodeCondition" =>
                  params = Map(
                    ParamName("node") -> ParamValue.Node(NodeId(targetId)),
                    ParamName("status") -> SelectedListValue(
                      status.asInstanceOf[BigInt].toInt match {
                        case 0 => "not visited";
                        case 1 => "visited";
                        case 2 => "is previous";
                        case 3 => "is not previous";
                        case 4 => "current"
                      }
                    )
                  )

                case "LinkCondition" =>
                  params = Map(
                    ParamName("link") -> ParamValue.Link(RulesetId(targetId)),
                    ParamName("status") -> SelectedListValue(
                      if (status.asInstanceOf[BigInt] == BigInt(1)) "followed" else "not followed"
                    )
                  )

                case "BooleanFactValue" =>

                  if (status.isInstanceOf[BigInt]) {
                    status = if (status == BigInt(1)) true else false
                  }
                  params = Map(
                    ParamName("fact") -> ParamValue.BooleanFact(FactId(targetId)),
                    ParamName("state") ->
                      SelectedListValue(if (status.asInstanceOf[Boolean]) "true" else "false")
                  )

                case "IntegerFactComparison" =>
                  val numFactArguments = numFactArgs.asInstanceOf[List[Any]].iterator
                  //val listString =
                  numFactArguments.next().asInstanceOf[String]
                  val operator = numFactArguments.next().asInstanceOf[String]
                  val mode = numFactArguments.next().asInstanceOf[String]
                  val value = numFactArguments.next().asInstanceOf[String]

                  val paramV =
                    if (mode == "Fact")
                      ParamValue.IntegerFact(FactId(BigInt(value)))
                    else
                      ParamValue.IntegerInput(BigInt(value))

                  params = Map(
                    ParamName("fact") -> ParamValue.IntegerFact(FactId(targetId)),
                    ParamName(if (mode == "Fact") "otherFact" else "input") -> paramV,
                    ParamName("operator") -> SelectedListValue(operator),
                    ParamName("comparisonValue") ->
                      UnionValueSelected(if (mode == "Fact") "otherFact" else "input")
                  )

                case _ =>
              }

              val newCondition = Condition(ConditionType(newConditionType), params)
              val newConditions = newCondition :: rule.conditions
              val newRule = rule
                .copy(rule.id, rule.name, rule.stopIfTrue, rule.conditionsOp, newConditions, rule.actions)
              val newRules = node.rules.map { i => if (i == rule) newRule else i }

              val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)

              return Option(newNode)
            }
          })
        })
      } else {
        node.content.rulesets.foreach(ruleSet => {
          ruleSet.rules.filter(_.id == RuleId(ruleId)).foreach(rule => {
            val definitions = ConditionDefinitions()

            definitions.foreach(definition => {
              if (definition.conditionType == ConditionType(newConditionType)) {
                var params: Map[ParamName, ParamValue] = Map()
                newConditionType match {
                  case "NodeCondition" =>
                    params = Map(
                      ParamName("node") -> ParamValue.Node(NodeId(targetId)),
                      ParamName("status") -> SelectedListValue(status.asInstanceOf[BigInt].toInt match {
                        case 0 => "not visited";
                        case 1 => "visited";
                        case 2 => "is previous";
                        case 3 => "is not previous";
                        case 4 => "current"
                      })
                    )

                  case "LinkCondition" =>
                    params = Map(
                      ParamName("link") -> ParamValue.Link(RulesetId(targetId)),
                      ParamName("status") -> SelectedListValue(
                        if (status.asInstanceOf[BigInt] == BigInt(1)) "followed" else "not followed"
                      )
                    )

                  case "BooleanFactValue" =>

                    if (status.isInstanceOf[BigInt]) {
                      status = if (status == BigInt(1)) true else false
                    }

                    params = Map(
                      ParamName("fact") -> ParamValue.BooleanFact(FactId(targetId)),
                      ParamName("state") ->
                        SelectedListValue(if (status.asInstanceOf[Boolean]) "true" else "false")
                    )

                  case "IntegerFactComparison" =>
                    val numFactArguments = numFactArgs.asInstanceOf[List[Any]].iterator
                    //val listString =
                    numFactArguments.next().asInstanceOf[String]
                    val operator = numFactArguments.next().asInstanceOf[String]
                    val mode = numFactArguments.next().asInstanceOf[String]
                    val value = numFactArguments.next().asInstanceOf[String]

                    val paramV =
                      if (mode == "Fact")
                        ParamValue.IntegerFact(FactId(BigInt(value)))
                      else
                        ParamValue.IntegerInput(BigInt(value))

                    params = Map(
                      ParamName("fact") -> ParamValue.IntegerFact(FactId(targetId)),
                      ParamName(if (mode == "Fact") "otherFact" else "input") -> paramV,
                      ParamName("operator") -> SelectedListValue(operator),
                      ParamName("comparisonValue") ->
                        UnionValueSelected(if (mode == "Fact") "otherFact" else "input")
                    )

                  case _ =>
                }

                val newCondition = Condition(ConditionType(newConditionType), params)
                val newConditions: List[Condition] = newCondition :: rule.conditions
                val newRule = rule.copy(
                  rule.id,
                  rule.name,
                  rule.stopIfTrue,
                  rule.conditionsOp,
                  newConditions,
                  rule.actions
                )
                val newRules: List[Rule] = ruleSet.rules.map { i => if (i == rule) newRule else i }
                val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                val newContent = node.content.copy(node.content.text, newFullSet)
                val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                return Option(newNode)
              }
            })
          })
        })
      }
    }

    Option.empty[Node]
  }

  /**
   * Processes the second level of tokens
   *
   * @param iterator     The token iterator
   * @param currentNode  The latest node to be processed
   * @param isWithinNode Flag indicating if the action should occur at the Node level or Fragment level
   */
  private def processSecondLevel(iterator: Iterator[Any],
                                 currentNode: Option[Node],
                                 isWithinNode: Boolean): Option[Node] = {
    var newCurrentNode = currentNode

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
                case "create-typed-rule3" =>
                  newCurrentNode = processCreateTypedRule3(i, newCurrentNode, isWithinNode)
                case "create-action" =>
                  newCurrentNode = processCreateAction(i, newCurrentNode)
                case "create-typed-condition2" =>
                  newCurrentNode = processCreateTypedCondition2(i, newCurrentNode, isWithinNode)
                case _ =>
              }
          }
      }
    }

    newCurrentNode
  }

  /**
   * Processes the link creation tokens
   *
   * @param i The token iterator
   * @return The updated story containing the node with the created link
   */
  private def processCreateLink(i: Iterator[Any], story: Story): Option[Node] = {

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

    story.nodes.filter(_.id == NodeId(fromNodeID)).foreach(node => {

      val newSet = Ruleset(
        RulesetId(linkId),
        linkName,
        RulesetIndexes(TextIndex(startIndex), TextIndex(endIndex)),
        List()
      )

      val newContent = node.content.copy(node.content.text, node.content.rulesets.:+(newSet))
      val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

      return Option(newNode)
    })

    Option.empty[Node]
  }

  /**
   * Processes node creation tokens
   *
   * @param i The token iterator
   * @return The created node
   */
  private def processCreateNode(i: Iterator[Any]): Node = {
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
    val isStartNode = false

    val node = Node(
      NodeId(nodeId),
      nameValue,
      NodeContent(contentText, List()),
      isStartNode,
      ruleSet
    )

    val newPosMap = AstMap("id" -> AstInteger(node.id.value), "x" -> AstFloat(x), "y" -> AstFloat(y))
    val newElems = nodePositions.toList.::(newPosMap)
    nodePositions = AstList(newElems: _*)

    node
  }

  /**
   * Process the tokens for create-fact action
   *
   * @param i     The token iterator
   * @param story The existing story to process the tokens into
   * @return The updated story
   */
  private def processCreateFact(i: Iterator[Any], story: Story): Story = {

    var newStory = story

    while (i.hasNext) {
      val factName = i.next().asInstanceOf[String]
      val factType = i.next().asInstanceOf[List[Any]].lift(1).get.asInstanceOf[String]
      val factId = FactId(i.next().asInstanceOf[BigInt])

      var fact = Option.empty[Fact]

      factType match {
        case "boolean" => val boolValue = false; fact = Option(BooleanFact(factId, factName, boolValue))
        case "string" => fact = Option(StringFact(factId, factName, ""))
        case "number" => fact = Option(IntegerFact(factId, factName, 0))
      }

      newStory = newStory.addFact(fact.get)
    }

    newStory
  }

  /**
   * Processes the first level of tokens
   *
   * @param iterator The token iterator
   * @param story    The existing story to process the tokens into
   * @return The updated story
   */
  private def processFirstLevel(iterator: Iterator[Any], story: Story): Story = {

    var newStory = story
    var startNode = BigInt(-1)

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
                case "make-hypertext" => newStory = processHeader(i, newStory)
                case "set-story-title!" => newStory = newStory.copy(i.next().asInstanceOf[String])
                case "set-author-name!" => newStory = newStory.changeAuthor(i.next().asInstanceOf[String])
                case "set-story-comment!" =>
                  newStory = newStory.updateMetadata(newStory.metadata.copy(i.next().asInstanceOf[String]))
                case "set-disable-back-button!" =>
                  newStory =
                    newStory.updateMetadata(
                      newStory.metadata.copy(
                        newStory.metadata.comments,
                        newStory.metadata.readerStyle,
                        i.next().asInstanceOf[Boolean]
                      )
                    )
                case "set-disable-restart-button!" =>
                  newStory =
                    newStory.updateMetadata(
                      newStory.metadata.copy(
                        newStory.metadata.comments,
                        newStory.metadata.readerStyle,
                        newStory.metadata.isBackButtonDisabled,
                        i.next().asInstanceOf[Boolean]
                      )
                    )
                case "set-start-node!" => startNode = i.next().asInstanceOf[BigInt]
                case "create-fact" => newStory = processCreateFact(i, newStory)
                case "begin" => headers.enqueue(current.asInstanceOf[List[Any]])
                case _ => // Console.println("Base: " + instruction)
              }
          }
      }
    }

    while (headers.nonEmpty) {
      val i = headers.dequeue().iterator
      newStory = processHeader(i, newStory)
    }

    newStory.nodes.filter(_.id == NodeId(startNode)).foreach(node => {
      val isStartNode = true
      val newNode = node.copy(node.id, node.name, node.content, isStartNode, node.rules)
      newStory = newStory.removeNode(node)
      newStory = newStory.addNode(newNode)
    })

    newStory
  }
}
