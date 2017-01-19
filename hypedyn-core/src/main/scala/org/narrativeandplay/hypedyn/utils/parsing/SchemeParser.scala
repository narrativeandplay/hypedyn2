package org.narrativeandplay.hypedyn.utils.parsing

import fastparse.all._
import fastparse.core.Parsed

import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetId, RulesetIndexes, TextIndex}
import org.narrativeandplay.hypedyn.story._
import org.narrativeandplay.hypedyn.story.internal.NodeContent.Ruleset
import org.narrativeandplay.hypedyn.story.internal.Story.Metadata
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamValue.{SelectedListValue, UnionValueSelected}
import org.narrativeandplay.hypedyn.story.rules.RuleLike.{ParamName, ParamValue}
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, _}
import org.narrativeandplay.hypedyn.story.rules.{BooleanOperator, _}
import org.narrativeandplay.hypedyn.utils.parsing.SchemeParser.Scheme._

/**
 * Parser for Hypedyn 1 files
 *
 * Parses Hypedyn 1 files into the new Hypedyn 2 format
 */
object SchemeParser {
  /**
   * Parses a string into a Story
   *
   * @param s The input string to parse
   * @return The parsed story
   */
  def parse(s: String): Map[String, Object] = {
    val Whitespace = NamedFunction(" \r\n\t".contains(_: Char), "Whitespace")
    val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
    val StringLiteralChars = NamedFunction(!"\"\\".contains(_: Char), "StringLiteralChars")
    val StringChars = NamedFunction(!" \r\n\t)".contains(_: Char), "StringChars")

    val space = P(CharsWhile(Whitespace).?)
    val digits = P(CharsWhile(Digits))
    val fractional = P("." ~ digits)
    val integral = P("0" | CharIn('1' to '9') ~ digits.?)

    val number = P(CharIn("+-").? ~ integral).!.map(x => Num(BigInt(x)))
    val double = P(integral ~ fractional).!.map(x => Doub(x.toDouble))
    val trueValue = P("#t" | "true") map (v => Bool(true))
    val falseValue = P("#f" | "false") map (v => Bool(false))

    val escape = P("\\" ~ CharIn("\"/\\bfnrt").!)

    val strLiteralChars = P(CharsWhile(StringLiteralChars))
    val strChars = P(CharsWhile(StringChars))
    val string = P(space ~ strChars.!).map(Str)
    val stringLiteral = P(space ~ "\"" ~ (strLiteralChars | escape).rep.?.! ~ "\"")
      .map(s => Str(StringContext treatEscapes s))
    lazy val quotedVal = P("(quote" ~ expression ~ ")")

    lazy val obj = P("(" ~ string ~ expression.rep ~ ")").map(Block)
    lazy val expression: Parser[Val] = P(
      space ~ (quotedVal | obj | trueValue | falseValue | double | number | stringLiteral | string) ~ space
    )

    val mainBlock = P("(begin" ~ (space ~ obj).rep ~ space ~ ")")

    val (nodePositions, story) = mainBlock.parse(s) match {
      case f: Parsed.Failure => (AstList(), Story())
      case s: Parsed.Success[Seq[Block]] => processBlocks(s.value)
    }

    val mapFields: AstMap = AstMap("zoomLevel" -> AstFloat(1.0), "nodes" -> nodePositions)
    val pluginData: AstElement = AstMap("Default Story Viewer" -> mapFields)

    Map("story" -> story, "plugins" -> pluginData)
  }

  /**
   * Builds node rules given a sequence of blocks
   *
   * @param story Story to add the built nodes to
   * @param blocks Sequence of blocks to read from
   * @return Story containing built node rules
   */
  private def buildNodeRules(story: Story, blocks: Seq[Seq[(Str, Seq[Block])]]) = {
    val relevantBlocks = blocks.filter(_.head._1.as[String] == "create-node")

    val isNodeRule = true
    relevantBlocks.foldLeft(story)((r, params) => {
      val nodeParams = params.head._2
      val nodeId = nodeParams(6).as[BigInt]
      buildRules(r, params.filter(_._1.as[String] == "begin"), nodeId, isNodeRule)
    })
  }

  /**
   * Build rules given a sequence of blocks
   *
   * @param story Story to add the built rules to
   * @param blocks Sequence of blocks to read from
   * @param nodeId The identifier of the node which the rules belong under
   * @param isNodeRule Flag that indicates if the rule is directly under the node (true) or under the node's content
   * @return Story containing built node rules
   */
  private def buildRules(story: Story, blocks: Seq[(Str, Seq[Block])], nodeId: BigInt, isNodeRule: Boolean): Story = {
    val relevantBlocks = blocks.filter(_._1.as[String] == "begin").map(_._2)
    relevantBlocks.foldLeft(story)((embR, embC) =>
      embC.foldLeft(embR)((embR2, embC2) => {
        val block = embC2.as[(Str, Seq[Val])]
        val linkParams = block._2
        block._1.as[String] match {
          case "create-typed-rule3" => createTypedRule3(nodeId, linkParams, embR2, isNodeRule)
          case "create-action" => createAction(nodeId, linkParams, embR2, isNodeRule)
          case "create-typed-condition2" => createTypedCondition2(nodeId, linkParams, embR2, isNodeRule)
          case _ => embR2
        }
      })
    )
  }

  /**
   * Build links given a seqeuence of blocks
   *
   * @param story Story to add the built rules to
   * @param blocks Sequence of blocks to read from
   * @return Story containing built links
   */
  private def buildLinks(story: Story, blocks: Seq[Seq[(Str, Seq[Block])]]): Story = {
    val linkOptions = blocks.filter(_.head._1.as[String] == "create-link")
    linkOptions.foldLeft(story)((r, c) => {
      val linkParams = c.head._2
      val name = linkParams.head.as[String]
      val nodeId = linkParams(1).as[BigInt]
      val startIndex = linkParams(3).as[BigInt]
      val endIndex = linkParams(4).as[BigInt]
      val linkId = linkParams(11).as[BigInt]

      r.nodes.find(_.id == NodeId(nodeId)) match {
        case None => r
        case Some(node) =>
          val newSet = Ruleset(
            RulesetId(linkId),
            name,
            RulesetIndexes(TextIndex(startIndex), TextIndex(endIndex)),
            List.empty
          )

          val newContent = node.content.copy(node.content.text, node.content.rulesets.:+(newSet))
          val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

          val newR = r.removeNode(node).addNode(newNode)

          val isNodeRule = false
          buildRules(newR, c, nodeId, isNodeRule)
      }
    })
  }

  /**
   * Produces a story given a sequence of blocks
   *
   * @param blocks Sequence of blocks to read from
   * @return Story produced from the sequence of blocks provided
   */
  private def processBlocks(blocks: Seq[Block]): (AstList, Story) = {
    val comments = getParam("set-story-comment!", blocks).getOrElse(Str()).as[String]
    val isBackButtonDisabled = getParam("set-disable-back-button!", blocks).getOrElse(Bool()).as[Boolean]
    val isRestartButtonDisabled = getParam("set-disable-restart-button!", blocks).getOrElse(Bool()).as[Boolean]

    val metaData = Metadata(comments, Narrative.ReaderStyle.Standard, isBackButtonDisabled, isRestartButtonDisabled)

    val setAuthorName = getParam("set-author-name!", blocks).getOrElse(Str()).as[String]
    val setStoryTitle = getParam("set-story-title!", blocks).getOrElse(Str()).as[String]
    val startNodeId = getParam("set-start-node!", blocks).map(x => x.as[BigInt])

    val storyMeta = Story(setStoryTitle.toString).changeAuthor(setAuthorName).updateMetadata(metaData)
    val storyFact = buildFacts(storyMeta, blocks)
    val relevantBlocks = blocks.filter(_.value._1.value == "begin").map(_.value._2.map(_.as[(Str, Seq[Block])]))
    val (nodePositions, storyNodes) = buildNodes(storyFact, relevantBlocks, startNodeId)
    val storyNodeRules = buildNodeRules(storyNodes, relevantBlocks)

    (nodePositions, buildLinks(storyNodeRules, relevantBlocks))
  }

  /**
   * Gets parameter given a sequence of blocks
   *
   * @param name Name of parameter
   * @param blocks Sequence of blocks to read from
   * @return Option with parameter value or empty if parameter is not found
   */
  private def getParam(name: String, blocks: Seq[Block]): Option[Val] = {
    blocks find (param => param.value._1.value == name) map (_.value._2.head)
  }

  /**
   * Builds nodes given sequence of blocks
   *
   * @param story Story to add the built nodes to
   * @param blocks Sequence of blocks to read from
   * @param startNodeId Option containing the start node ID if any
   * @return Story containing built nodes
   */
  private def buildNodes(story: Story, blocks: Seq[Seq[(Str, Seq[Block])]],
                         startNodeId: Option[BigInt]): (AstList, Story) = {
    val relevantBlocks = blocks.filter(_.head._1.as[String] == "create-node").map(_.head._2)

    relevantBlocks.foldLeft((AstList(), story))((r, params) => {
      val name = params.head.as[String]
      val content = params(1).as[String]
      val x = params(2).as[Double]
      val y = params(3).as[Double]
      val nodeId = params(6).as[BigInt]

      val ruleSet: List[Rule] = List.empty
      val isStartNode = startNodeId match {
        case Some(v: BigInt) => v == nodeId
        case _ => false
      }

      val node = Node(
        NodeId(nodeId),
        name,
        NodeContent(content, List.empty),
        isStartNode,
        ruleSet
      )

      val newPosMap = AstMap("id" -> AstInteger(node.id.value), "x" -> AstFloat(x), "y" -> AstFloat(y))
      val newElems = r._1.toList.::(newPosMap)
      val nodePositions = AstList(newElems: _*)

      (nodePositions, r._2.addNode(node))
    })
  }

  /**
   * Creates a rule
   *
   * @param nodeId The identifier of the node which the rule belongs under
   * @param params Rule parameters
   * @param story Story to add the rule to
   * @param isNodeRule Flag that indicates if the rule is directly under the node (true) or under the node's content
   * @return Story containing built rule
   */
  private def createTypedRule3(nodeId: BigInt, params: Seq[Val], story: Story, isNodeRule: Boolean): Story = {
    val ruleName = params.head.as[String]
    val stringOperator = params(2).as[String]
    val boolOperator = if (stringOperator == "or") BooleanOperator.Or else BooleanOperator.And
    val linkId = params(4).as[BigInt]
    val fixedId = params(6).as[BigInt]
    val fallThrough = params(8).as[Boolean]

    val rule = Rule(RuleId(fixedId), ruleName, !fallThrough, boolOperator, List.empty, List.empty)

    story.nodes.find(_.id == NodeId(nodeId)) match {
      case Some(node) =>
        if (isNodeRule) {
          val newRules: List[Rule] = node.rules.:+(rule)
          val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)
          story.removeNode(node).addNode(newNode)
        } else {
          node.content.rulesets.find(_.id == RulesetId(linkId)) match {
            case Some(ruleSet) =>
              val newRules: List[Rule] = ruleSet.rules.:+(rule)

              val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
              val newFullSet: List[Ruleset] = node.content.rulesets.map(x => if (x == ruleSet) newSet else x)
              val newContent = node.content.copy(node.content.text, newFullSet)
              val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)
              story.removeNode(node).addNode(newNode)

            case None => story
          }
        }
      case None => story
    }
  }

  /**
   * Creates an rule action
   *
   * @param nodeId The identifier of the node which the rule action belongs under
   * @param params Rule action parameters
   * @param story Story to add the rule action to
   * @param isNodeRule Flag indicates if the rule action is directly under the node (true) or under the node's content
   * @return Story containing built rule action
   */
  private def createAction(nodeId: BigInt, params: Seq[Val], story: Story, isNodeRule: Boolean): Story = {
    val actionType = params(1).as[String]
    val expression = params(2).as[(Str, Seq[Val])]._2
    val parentRuleId = params(3).as[BigInt]

    story.nodes.find(_.id == NodeId(nodeId)) match {
      case Some(node) =>
        val actionOption = actionType match {
          case "clicked-link" =>
            expression.head.as[String] match {
              case "follow-link" =>
                processFollowLink(expression, parentRuleId, node)
              case "assert" =>
                val boolVal = true
                Option(processActionSetFact(expression, parentRuleId, boolVal))
              case "retract" =>
                val boolVal = false
                Option(processActionSetFact(expression, parentRuleId, boolVal))
              case "set-number-fact" => Option(processSetNumberFact(expression, parentRuleId))
              case "anywhere-check" => Option(processAnywhereCheck(expression, parentRuleId))
              case "set-value!" => Option(processSetValue(expression, parentRuleId))
              case "show-in-popup" => Option(processShowInPopup(expression, parentRuleId))
              case _ => Option.empty[Action]
            }
          case "entered-node" =>
            expression.head.as[String] match {
              case "retract" =>
                val boolValue = false
                Option(processActionSetFact(expression, parentRuleId, boolValue))
              case "assert" =>
                val boolValue = true
                Option(processActionSetFact(expression, parentRuleId, boolValue))
              case "set-number-fact" => Option(processSetNumberFact(expression, parentRuleId))
              case "anywhere-check" => Option(processAnywhereCheck(expression, parentRuleId))
              case "set-value!" => Option(processSetValue(expression, parentRuleId))
              case _ => Option.empty[Action]
            }
          case "anywhere-check" =>
            Option(processAnywhereCheck(expression, parentRuleId))
          case "displayed-node" =>
            Option(processDisplayedNode(expression, parentRuleId))
        }

        actionOption match {
          case Some(action: Action) =>
            if (isNodeRule) {
              node.rules.find(_.id == RuleId(parentRuleId)) match {
                case Some(rule) =>
                  val newActions = rule.actions.:+(action)
                  val newRule = rule.copy(
                    rule.id,
                    rule.name,
                    rule.stopIfTrue,
                    rule.conditionsOp,
                    rule.conditions,
                    newActions
                  )

                  val newRules: List[Rule] = node.rules.map { i => if (i.id == rule.id) newRule else i }

                  val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)
                  story.removeNode(node).addNode(newNode)
                case None => story
              }
            } else {
              val newRuleSets = node.content.rulesets map {
                ruleSet =>
                  ruleSet.rules.find(_.id == RuleId(parentRuleId)) match {
                    case Some(rule) =>
                      val newActions = rule.actions.:+(action)
                      val newRule = rule.copy(
                        rule.id,
                        rule.name,
                        rule.stopIfTrue,
                        rule.conditionsOp,
                        rule.conditions,
                        newActions
                      )
                      ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes,
                        ruleSet.rules.map(r => if (r.id == rule.id) newRule else r))
                    case None => ruleSet
                  }
              }
              val newNodeContent = node.content.copy(node.content.text, newRuleSets)
              val newNode = node.copy(node.id, node.name, newNodeContent, node.isStartNode, node.rules)
              story.removeNode(node).addNode(newNode)
            }
          case _ => story
        }
      case None => story
    }
  }

  /**
   * Process the tokens for the follow-link rule
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @param currentNode The latest node processed
   * @return
   */
  private def processFollowLink(expression: Seq[Val], parentRuleId: BigInt, currentNode: Node): Option[Action] = {
    Option(
      Action(
        ActionType("LinkTo"),
        Map(ParamName("node") -> ParamValue.Node(NodeId(expression(4).as[BigInt])))
      )
    )
  }

  /**
   * Processes the tokens for the set-number-fact action
   *
   * @param expression The combination of tokens that make up an expression
   * @param ruleId The rule identifier
   * @return The created action
   */
  private def processSetNumberFact(expression: Seq[Val], ruleId: BigInt): Action = {
    val factId = expression(1).as[BigInt]
    val setOption = expression(2).as[String]

    val params = setOption match {
      case "Input" =>
        val setFactId = expression(1).as[BigInt]
        val setFactValue = expression(3).as[String]

        Map(ParamName("updateValue") -> ParamValue.UnionValueSelected("inputValue"),
          ParamName("fact") -> ParamValue.IntegerFact(FactId(setFactId)),
          ParamName("inputValue") -> ParamValue.IntegerInput(BigInt(setFactValue)))
      case "Math" =>
        val optionExpression = expression(3).as[(Str, Seq[Val])]._2
        val eOperator = optionExpression.head.as[String]
        val value1 = optionExpression(1).as[String]
        val eOption1 = optionExpression(2).as[String]
        val value2 = optionExpression(3).as[String]
        val eOption2 = optionExpression(4).as[String]

        val operand1IsFact = if (eOption1 == "Fact") true else false
        val operand2IsFact = if (eOption2 == "Fact") true else false

        val operand1 = if (operand1IsFact) "factOperand1" else "userOperand1"
        val operand2 = if (operand2IsFact) "factOperand2" else "userOperand2"

        Map(ParamName("fact") -> ParamValue.IntegerFact(FactId(factId)),
          ParamName("updateValue") -> ParamValue.UnionValueSelected("computation"),
          ParamName("operand1") -> ParamValue.UnionValueSelected(operand1),
          if (operand1IsFact) {
            ParamName(operand1) -> ParamValue.IntegerFact(FactId(BigInt(value1)))
          } else {
            ParamName(operand1) -> ParamValue.IntegerInput(BigInt(value1))
          },
          ParamName("operator") -> ParamValue.SelectedListValue(eOperator),
          ParamName("operand2") -> ParamValue.UnionValueSelected(operand2),

          if (operand2IsFact) {
            ParamName(operand2) -> ParamValue.IntegerFact(FactId(BigInt(value2)))
          } else {
            ParamName(operand2) -> ParamValue.IntegerInput(BigInt(value2))
          },
          ParamName("computation") -> ParamValue.ProductValue(List("operand1", "operator", "operand2"))
        )
      case "Fact" =>
        val setFromFactId = expression(1).as[BigInt]
        val setToFactId = expression(3).as[BigInt]
        Map(ParamName("updateValue") -> ParamValue.UnionValueSelected("integerFactValue"),
          ParamName("fact") -> ParamValue.IntegerFact(FactId(setFromFactId)),
          ParamName("integerFactValue") -> ParamValue.IntegerFact(FactId(setToFactId)))
      case "Random" =>
        val optionExpression = expression(3).as[(Str, Seq[Block])]._2
        val value1 = optionExpression.head.as[String]
        val eOption1 = optionExpression(1).as[String]
        val value2 = optionExpression(2).as[String]
        val eOption2 = optionExpression(3).as[String]

        val operand1IsFact = if (eOption1 == "Fact") true else false
        val operand2IsFact = if (eOption2 == "Fact") true else false

        val operand1 = if (operand1IsFact) "startFact" else "startInput"
        val operand2 = if (operand2IsFact) "endFact" else "endInput"

        Map(ParamName("randomValue") -> ParamValue.ProductValue(List("start", "end")),
          ParamName("updateValue") -> ParamValue.UnionValueSelected("randomValue"),
          ParamName("fact") -> ParamValue.IntegerFact(FactId(factId)),
          ParamName("start") -> ParamValue.UnionValueSelected(operand1),
          if (operand1IsFact) {
            ParamName(operand1) -> ParamValue.IntegerFact(FactId(BigInt(value1)))
          } else {
            ParamName(operand1) -> ParamValue.IntegerInput(BigInt(value1))
          },
          ParamName("end") -> ParamValue.UnionValueSelected(operand2),
          if (operand2IsFact) {
            ParamName(operand2) -> ParamValue.IntegerFact(FactId(BigInt(value2)))
          } else {
            ParamName(operand2) -> ParamValue.IntegerInput(BigInt(value2))
          })
      case _ => Map.empty[ParamName, ParamValue]
    }

    Action(ActionType("UpdateIntegerFacts"), params)
  }

  /**
   * Processes the tokens that set a boolean fact
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @param boolValue The value to set the boolean fact to
   * @return The created action
   */
  private def processActionSetFact(expression: Seq[Val], parentRuleId: BigInt, boolValue: Boolean): Action = {
    val factId = expression(1).as[BigInt]
    val params = Map(
      ParamName("fact") -> ParamValue.BooleanFact(FactId(factId)),
      ParamName("value") -> SelectedListValue(if (boolValue) "true" else "false")
    )

    Action(ActionType("UpdateBooleanFact"), params)
  }

  /**
   * Processes the tokens that enables anywhere links to a node
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processAnywhereCheck(expression: Seq[Val], parentRuleId: BigInt): Action = {
    Action(ActionType("EnableAnywhereLinkToHere"), Map.empty[ParamName, ParamValue])
  }

  /**
   * Processes the tokens for the show-in-popup action
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processShowInPopup(expression: Seq[Val], parentRuleId: BigInt): Action = {
    Action(ActionType("ShowPopupNode"), Map(ParamName("node") -> ParamValue.Node(NodeId(expression(1).as[BigInt]))))
  }

  /**
   * Processes the tokens for the displayed-node action
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processDisplayedNode(expression: Seq[Val], parentRuleId: BigInt): Action = {
    val factType = expression(1).as[String]
    val value = expression(2)

    val params = factType match {
      case "alternative text" =>
        Map(
          ParamName("textInput") -> ParamValue.StringInput(value.as[String]),
          ParamName("text") -> UnionValueSelected("textInput")
        )
      case "text fact" =>
        Map(
          ParamName("stringFactValue") -> ParamValue.StringFact(FactId(value.as[BigInt])),
          ParamName("text") -> UnionValueSelected("stringFactValue")
        )
      case "number fact" =>
        Map(
          ParamName("NumberFactValue") -> ParamValue.IntegerFact(FactId(value.as[BigInt])),
          ParamName("text") -> UnionValueSelected("NumberFactValue")
        )
      case _ => Map.empty[ParamName, ParamValue]
    }

    Action(ActionType("UpdateText"), params)
  }

  /**
   * Processes the tokens for the set-value! action
   *
   * @param expression The combination of tokens that make up an expression
   * @param parentRuleId The parent rule identifier
   * @return The created action
   */
  private def processSetValue(expression: Seq[Val], parentRuleId: BigInt): Action = {
    val factId = expression(1).as[BigInt]
    val value = expression(2).as[String]

    val params = Map(
      ParamName("fact") -> ParamValue.StringFact(FactId(factId)),
      ParamName("value") -> ParamValue.StringInput(value)
    )

    Action(ActionType("UpdateStringFact"), params)
  }

  def matchCondition(newConditionType: String, targetId: BigInt, status: Val,
                     numFactArgs: Val): Map[ParamName, ParamValue] = {
    newConditionType match {
      case "NodeCondition" =>
        Map(
          ParamName("node") -> ParamValue.Node(NodeId(targetId)),
          ParamName("status") -> SelectedListValue(status.value match {
            case 0 => "not visited"
            case 1 => "visited"
            case 2 => "is previous"
            case 3 => "is not previous"
            case 4 => "current"
          })
        )
      case "LinkCondition" =>
        Map(
          ParamName("link") -> ParamValue.Link(RulesetId(targetId)),
          ParamName("status") -> SelectedListValue(
            if (status.value == BigInt(1)) "followed" else "not followed"
          )
        )
      case "BooleanFactValue" =>
        val boolStatus = status.value match {
          case bigInt: BigInt if bigInt == BigInt(1) => true
          case bigInt: BigInt if bigInt == BigInt(0) => false
          case boolean: Boolean => boolean
          case _ => false
        }
        Map(
          ParamName("fact") -> ParamValue.BooleanFact(FactId(targetId)),
          ParamName("state") ->
            SelectedListValue(if (boolStatus) "true" else "false")
        )
      case "IntegerFactComparison" =>
        val numFactArguments = numFactArgs.as[(Str, Seq[Block])]._2
        val operator = numFactArguments.head.as[String]
        val mode = numFactArguments(1).as[String]
        val value = numFactArguments(2).as[String]

        val paramV =
          if (mode == "Fact") ParamValue.IntegerFact(FactId(BigInt(value))) else ParamValue.IntegerInput(BigInt(value))

        Map(
          ParamName("fact") -> ParamValue.IntegerFact(FactId(targetId)),
          ParamName(if (mode == "Fact") "otherFact" else "input") -> paramV,
          ParamName("operator") -> SelectedListValue(operator),
          ParamName("comparisonValue") ->
            UnionValueSelected(if (mode == "Fact") "otherFact" else "input")
        )
      case _ => Map.empty[ParamName, ParamValue]
    }
  }

  /**
   * Process the tokens for create-typed-condition-2 rule
   *
   * @param nodeId The identifier of the node which the rules belong under
   * @param blocks Sequence of blocks to read from
   * @param story Story to add the built rules to
   * @param isNodeRule Flag indicates if the rule is directly under the node (true) or under the node's content
   * @return
   */
  private def createTypedCondition2(nodeId: BigInt, blocks: Seq[Val], story: Story, isNodeRule: Boolean): Story = {
    val newConditionType = blocks(1).as[BigInt].toInt match {
      case 0 => "NodeCondition"
      case 1 => "LinkCondition"
      case 2 => "BooleanFactValue"
      case 3 => "IntegerFactComparison"
      case _ => ""
    }

    val targetId = blocks(2).as[BigInt]
    val status = blocks(3)
    val ruleId = blocks(4).as[BigInt]
    val numFactArgs = blocks(8)

    val params = matchCondition(newConditionType, targetId, status, numFactArgs)
    val newCondition = Condition(ConditionType(newConditionType), params)
    story.nodes.find(_.id == NodeId(nodeId)) match {
      case Some(node) =>
        if (isNodeRule) {
          node.rules.find(_.id == RuleId(ruleId)) match {
            case Some(rule) =>
              val newConditions: List[Condition] = newCondition :: rule.conditions
              val newRule = rule.copy(
                rule.id,
                rule.name,
                rule.stopIfTrue,
                rule.conditionsOp,
                newConditions,
                rule.actions
              )
              val newRules: List[Rule] = node.rules.map { i => if (i.id == rule.id) newRule else i }
              val newNode = node.copy(node.id, node.name, node.content, node.isStartNode, newRules)
              story.removeNode(node).addNode(newNode)
            case None => story
          }
        } else {
          node.content.rulesets.find(_.rules.exists(_.id == RuleId(ruleId))) match {
            case Some(ruleSet) =>
              ruleSet.rules.find(_.id == RuleId(ruleId)) match {
                case Some(rule) =>
                  val newConditions: List[Condition] = newCondition :: rule.conditions
                  val newRule = rule.copy(
                    rule.id,
                    rule.name,
                    rule.stopIfTrue,
                    rule.conditionsOp,
                    newConditions,
                    rule.actions
                  )

                  val newRules: List[Rule] = ruleSet.rules.map { i => if (i.id == rule.id) newRule else i }

                  val newSet: Ruleset = ruleSet.copy(ruleSet.id, ruleSet.name, ruleSet.indexes, newRules)
                  val newFullSet: List[Ruleset] = node.content.rulesets.map(i => if (i == ruleSet) newSet else i)
                  val newContent = node.content.copy(node.content.text, newFullSet)
                  val newNode = node.copy(node.id, node.name, newContent, node.isStartNode, node.rules)

                  story.removeNode(node).addNode(newNode)
                case None => story
              }
            case None => story
          }
        }
      case None => story
    }
  }

  /**
   * Builds node facts given a sequence of blocks
   *
   * @param story Story to add the built nodes to
   * @param blocks Sequence of blocks to read from
   * @return Story containing built node facts
   */
  private def buildFacts(story: Story, blocks: Seq[Block]): Story = {
    val relevantBlocks = blocks.filter(_.value._1.value == "create-fact").map(_.value._2)

    relevantBlocks.foldLeft(story)((r, params) => {
      val factName = params.head.as[String]
      val factType = params(1).as[String]
      val factId = FactId(params(2).as[BigInt])

      val newFact = factType match {
        case "boolean" => val boolValue = false; Option(BooleanFact(factId, factName, boolValue))
        case "string" => Option(StringFact(factId, factName, ""))
        case "number" => Option(IntegerFact(factId, factName, 0))
        case _ => None
      }

      newFact match {
        case Some(fact) => r.addFact(fact)
        case None => r
      }
    })
  }

  /**
   * Wrapper for a named function
   *
   * @param f Anonymous function
   * @param name Name of the function
   * @tparam T Parameter type
   * @tparam V Return type
   */
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V) {
    def apply(t: T) = f(t)

    override def toString() = name
  }

  /**
   * Internal value class wrapper for data types
   */
  object Scheme {
    sealed trait Val extends Any {
      def value: Any

      def as[T] = value.asInstanceOf[T]
    }
    case class Str(value: java.lang.String = "") extends AnyVal with Val
    case class Block(value: (Str, Seq[Val])) extends AnyVal with Val {
      def _1: Str = value._1

      def _2: Seq[Val] = value._2
    }
    case class Doub(value: Double) extends AnyVal with Val
    case class Num(value: BigInt) extends AnyVal with Val
    case class Bool(value: Boolean = false) extends AnyVal with Val
  }
}
