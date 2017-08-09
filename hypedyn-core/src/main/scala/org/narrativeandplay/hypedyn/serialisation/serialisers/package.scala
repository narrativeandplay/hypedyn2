package org.narrativeandplay.hypedyn.serialisation

import org.narrativeandplay.hypedyn.story.internal.NodeContent.Ruleset
import org.narrativeandplay.hypedyn.story.internal.Story.Metadata
import org.narrativeandplay.hypedyn.story.internal.{Node, NodeContent, Story}
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, Condition, Rule}
import scala.reflect.ClassTag

import org.narrativeandplay.hypedyn.api.serialisation._
import org.narrativeandplay.hypedyn.api.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.api.story.NodalContent.RulesetId
import org.narrativeandplay.hypedyn.api.story.NodeId
import org.narrativeandplay.hypedyn.api.story.rules.BooleanOperator.{And, Or}
import org.narrativeandplay.hypedyn.api.story.rules._
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.ParamValue

package object serialisers {

  /**
   * Downcasts `AstElement`s to the specified instance of `AstElement`. Throws a `DeserialisationException`, which is more
   * useful than simply throwing a `ClassCastException`
   *
   * @param astElement The `AstElement` to cast
   * @param deserialisationTarget A string denoting what is being deserialised, for more descriptive exceptions
   * @param ev The `ClassTag` of the result type
   * @tparam T The `AstElement` subtype to cast to
   * @throws DeserialisationException When the object being cast is not of the specified type `T`
   * @return The downcasted `AstElement`
   */
  @throws(classOf[DeserialisationException])
  private def safeCast[T <: AstElement](astElement: AstElement, deserialisationTarget: String)
                                       (implicit ev: ClassTag[T]): T = astElement match {
    case elem: T => elem
    case err => throw DeserialisationException(
      s"Expected ${ev.runtimeClass.toString} when deserialising $deserialisationTarget, " +
        s"received ${err.getClass.getSimpleName} instead.")
  }

  /**
   * Typeclass instance for serialising Nodes
   */
  implicit object NodeSerialiser extends Serialisable[Node] {
    /**
     * Returns the serialised representation of an object
     *
     * @param node The object to serialise
     */
    override def serialise(node: Node): AstElement =
      AstMap("id" -> AstInteger(node.id.value),
             "name" -> AstString(node.name),
             "content" -> NodeContentSerialiser.serialise(node.content),
             "isStart" -> AstBoolean(node.isStartNode),
             "rules" -> AstList((node.rules map RuleSerialiser.serialise).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Node = {
      def safeCastNode[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) = safeCast[T](astElement, "node")

      val data = safeCastNode[AstMap](serialised)
      val id = safeCastNode[AstInteger](data("id")).i
      val name = safeCastNode[AstString](data("name")).s
      val content = NodeContentSerialiser.deserialise(data("content"))
      val isStart = safeCastNode[AstBoolean](data("isStart")).boolean
      val rules = safeCastNode[AstList](data("rules")).toList map RuleSerialiser.deserialise

      new Node(NodeId(id), name, content, isStart, rules)
    }
  }

  /**
   * Implicit class to allow the use of `node.serialise`
   *
   * @param node The node to extend
   */
  implicit class SerialisableNode(node: Node) {
    def serialise = NodeSerialiser.serialise(node)
  }

  /**
   * Typeclass instance for serialising stories
   */
  implicit object StorySerialiser extends Serialisable[Story] {
    /**
     * Returns the serialised representation of an object
     *
     * @param story The object to serialise
     */
    override def serialise(story: Story): AstElement =
      AstMap("title" -> AstString(story.metadata.title),
             "author" -> AstString(story.metadata.author),
             "description" -> AstString(story.metadata.description),
             "metadata" -> StoryMetadataSerialiser.serialise(story.metadata),
             "nodes" -> AstList(story.nodes map NodeSerialiser.serialise: _*),
             "facts" -> AstList(story.facts map FactSerialiser.serialise: _*),
             "rules" -> AstList(story.rules map RuleSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Story = {
      def safeCastStory[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) = safeCast[T](astElement, "story")

      val data = safeCastStory[AstMap](serialised)
      val title = safeCastStory[AstString](data("title")).s
      val author = safeCastStory[AstString](data("author")).s
      val description = safeCastStory[AstString](data("description")).s
      val metadata = StoryMetadataSerialiser.deserialise(data("metadata"))
      val nodes = safeCastStory[AstList](data("nodes")).toList map NodeSerialiser.deserialise
      val facts = safeCastStory[AstList](data("facts")).toList map FactSerialiser.deserialise
      val rules = safeCastStory[AstList](data("rules")).toList map RuleSerialiser.deserialise

      new Story(metadata.copy(title = title, author = author, description = description), nodes, facts, rules)
    }
  }

  /**
   * Implicit class to allow `story.serialise`
   *
   * @param story The story to extend
   */
  implicit class SerialisableStory(story: Story) {
    def serialise = StorySerialiser.serialise(story)
  }

  /**
   * Typeclass instance for serialising node content
   */
  implicit object NodeContentSerialiser extends Serialisable[NodeContent] {
    /**
     * Returns the serialised representation of an object
     *
     * @param nodeContent The object to serialise
     */
    override def serialise(nodeContent: NodeContent): AstElement =
      AstMap("text" -> AstString(nodeContent.text),
             "rulesets" -> AstList(nodeContent.rulesets map RulesetSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): NodeContent = {
      def safeCastNodeContent[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "node content")

      val data = safeCastNodeContent[AstMap](serialised)
      val text = safeCastNodeContent[AstString](data("text")).s
      val rulesets = safeCastNodeContent[AstList](data("rulesets")).toList map RulesetSerialiser.deserialise

      NodeContent(text, rulesets)
    }
  }

  /**
   * Typeclass instance for serialising rulesets
   */
  implicit object RulesetSerialiser extends Serialisable[NodeContent.Ruleset] {
    /**
     * Returns the serialised representation of an object
     *
     * @param ruleset The object to serialise
     */
    override def serialise(ruleset: Ruleset): AstElement =
      AstMap("id" -> AstInteger(ruleset.id.value),
             "name" -> AstString(ruleset.name),
             "start" -> AstInteger(ruleset.indexes.startIndex.index),
             "end" -> AstInteger(ruleset.indexes.endIndex.index),
             "rules" -> AstList(ruleset.rules map RuleSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Ruleset = {
      def safeCastRuleset[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "ruleset")

      import org.narrativeandplay.hypedyn.api.story.NodalContent._
      val data = safeCastRuleset[AstMap](serialised)
      val id = RulesetId(safeCastRuleset[AstInteger](data("id")).i)
      val name = safeCastRuleset[AstString](data("name")).s
      val indexes = RulesetIndexes(TextIndex(safeCastRuleset[AstInteger](data("start")).i),
                                   TextIndex(safeCastRuleset[AstInteger](data("end")).i))
      val rules = safeCastRuleset[AstList](data("rules")).toList map RuleSerialiser.deserialise

      Ruleset(id, name, indexes, rules)
    }
  }

  /**
   * Typeclass instance for serialising rules
   */
  implicit object RuleSerialiser extends Serialisable[Rule] {
    /**
     * Returns the serialised representation of an object
     *
     * @param rule The object to serialise
     */
    override def serialise(rule: Rule): AstElement =
      AstMap("id" -> AstInteger(rule.id.value),
             "name" -> AstString(rule.name),
             "stopIfTrue" -> AstBoolean(rule.stopIfTrue),
             "conditionsOp" -> AstString(rule.conditionsOp match {
                                           case And => "and"
                                           case Or => "or"
                                         }),
             "conditions" -> AstList(rule.conditions map ConditionSerialiser.serialise: _*),
             "actions" -> AstList(rule.actions map ActionSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Rule = {
      def safeCastRule[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "rule")

      val data = safeCastRule[AstMap](serialised)
      val id = RuleId(safeCastRule[AstInteger](data("id")).i)
      val name = safeCastRule[AstString](data("name")).s
      val stopIfTrue = safeCastRule[AstBoolean](data("stopIfTrue")).boolean
      val conditionsOp = safeCastRule[AstString](data("conditionsOp")).s match {
        case "and" => And
        case "or" => Or
        case unknown => throw DeserialisationException(s"Unknown operator for conditons: $unknown")
      }
      val conditions = safeCastRule[AstList](data("conditions")).toList map ConditionSerialiser.deserialise
      val actions = safeCastRule[AstList](data("actions")).toList map ActionSerialiser.deserialise

      Rule(id, name, stopIfTrue, conditionsOp, conditions, actions)
    }
  }

  private def paramValueToAstMap(paramValue: ParamValue): AstMap =  {
    import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.ParamValue._
    paramValue match {
      case ParamValue.Node(n) => AstMap("type" -> AstString("node"),
                                        "value" -> AstInteger(n.value))
      case Link(l) => AstMap("type" -> AstString("link"),
                             "value" -> AstInteger(l.value))
      case IntegerFact(f) => AstMap("type" -> AstString("integerFact"),
                                    "value" -> AstInteger(f.value))
      case BooleanFact(f) => AstMap("type" -> AstString("booleanFact"),
                                    "value" -> AstInteger(f.value))
      case StringFact(f) => AstMap("type" -> AstString("stringFact"),
                                   "value" -> AstInteger(f.value))
      case StringInput(s) => AstMap("type" -> AstString("string"),
                                    "value" -> AstString(s))
      case IntegerInput(i) => AstMap("type" -> AstString("integer"),
                                     "value" -> AstInteger(i))
      case SelectedListValue(s) => AstMap("type" -> AstString("selectedListValue"),
                                          "value" -> AstString(s))
      case UnionValueSelected(p) => AstMap("type" -> AstString("union"),
                                           "value" -> AstString(p))
      case ProductValue(ns) => AstMap("type" -> AstString("product"),
                                      "value" -> AstString(ns mkString ":"))
    }
  }

  private def astMapToParamValue(astMap: AstMap): ParamValue = {
    def safeCastParamValue[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
      safeCast[T](astElement, "param value")

    val m = astMap.toMap

    // Sanity check
    assert(m.size == 2)
    assert(m contains "type")
    assert(m contains "value")

    import ParamValue._
    safeCastParamValue[AstString](m("type")).s match {
      case "node" => ParamValue.Node(NodeId(safeCastParamValue[AstInteger](m("value")).i))
      case "link" => Link(RulesetId(safeCastParamValue[AstInteger](m("value")).i))
      case "integerFact" => IntegerFact(FactId(safeCastParamValue[AstInteger](m("value")).i))
      case "booleanFact" => BooleanFact(FactId(safeCastParamValue[AstInteger](m("value")).i))
      case "stringFact" => StringFact(FactId(safeCastParamValue[AstInteger](m("value")).i))
      case "string" => StringInput(safeCastParamValue[AstString](m("value")).s)
      case "integer" => IntegerInput(safeCastParamValue[AstInteger](m("value")).i)
      case "selectedListValue" => SelectedListValue(safeCastParamValue[AstString](m("value")).s)
      case "union" => UnionValueSelected(safeCastParamValue[AstString](m("value")).s)
      case "product" => ProductValue((safeCastParamValue[AstString](m("value")).s split ":").toList)
      case unknown => throw DeserialisationException(s"Invalid type for ParamValue: $unknown")
    }
  }

  /**
   * Typeclass instance for serialsing conditions
   */
  implicit object ConditionSerialiser extends Serialisable[Condition] {
    /**
     * Returns the serialised representation of an object
     *
     * @param condition The object to serialise
     */
    override def serialise(condition: Condition): AstElement =
      AstMap("conditionType" -> AstString(condition.conditionType.value),
             "params" -> AstMap((condition.params map { case (k, v) =>
               k.value -> paramValueToAstMap(v)
             }).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Condition = {
      def safeCastCondition[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "condition")

      val data = safeCastCondition[AstMap](serialised)
      val conditionType = safeCastCondition[AstString](data("conditionType")).s
      val params = safeCastCondition[AstMap](data("params")).toMap map { case (k, v) =>
        k -> astMapToParamValue(safeCastCondition[AstMap](v))
      }

      Condition(Conditional.ConditionType(conditionType), params map { case (k, v) =>
        RuleLike.ParamName(k) -> v
      })
    }
  }

  /**
   * Typeclass instance for serialising actions
   */
  implicit object ActionSerialiser extends Serialisable[Action] {
    /**
     * Returns the serialised representation of an object
     *
     * @param action The object to serialise
     */
    override def serialise(action: Action): AstElement =
      AstMap("actionType" -> AstString(action.actionType.value),
             "params" -> AstMap((action.params map { case (k, v) =>
               k.value -> paramValueToAstMap(v)
             }).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Action = {
      def safeCastAction[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "action")

      val data = safeCastAction[AstMap](serialised)
      val actionType = safeCastAction[AstString](data("actionType")).s
      val params = safeCastAction[AstMap](data("params")).toMap map { case (k, v) =>
          k -> astMapToParamValue(v.asInstanceOf[AstMap])
      }

      Action(Actionable.ActionType(actionType), params map { case (k, v) =>
        RuleLike.ParamName(k) -> v
      })
    }
  }

  /**
   * Typeclass instance for serialising facts
   */
  implicit object FactSerialiser extends Serialisable[Fact] {
    /**
     * Returns the serialised representation of an object
     *
     * @param fact The object to serialise
     */
    override def serialise(fact: Fact): AstElement = {
      val value: AstElement = fact match {
        case IntegerFact(_, _, i) => AstInteger(i)
        case StringFact(_, _, s) => AstString(s)
        case BooleanFact(_, _, b) => AstBoolean(b)
        case IntegerFactList(_, _, is) => AstList(is map serialise: _*)
        case StringFactList(_, _, ss) => AstList(ss map serialise: _*)
        case BooleanFactList(_, _, bs) => AstList(bs map serialise: _*)
      }

      val factType = fact match {
        case _: IntegerFact => "int"
        case _: StringFact => "string"
        case _: BooleanFact => "bool"
        case _: IntegerFactList => "int list"
        case _: StringFactList => "string list"
        case _: BooleanFactList => "bool list"
      }

      AstMap("id" -> AstInteger(fact.id.value),
             "name" -> AstString(fact.name),
             "type" -> AstString(factType),
             "initialValue" -> value)
    }

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Fact = {
      def safeCastFact[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "fact")

      val data = safeCastFact[AstMap](serialised)
      val id = FactId(safeCastFact[AstInteger](data("id")).i)
      val name = safeCastFact[AstString](data("name")).s

      (safeCastFact[AstString](data("type")).s, data("initialValue")) match {
        case ("int", value) => IntegerFact(id, name, safeCastFact[AstInteger](value).i)
        case ("string", value) => StringFact(id, name, safeCastFact[AstString](value).s)
        case ("bool", value) => BooleanFact(id, name, safeCastFact[AstBoolean](value).boolean)
        case ("int list", value) =>
          IntegerFactList(
            id,
            name,
            safeCastFact[AstList](value).toList map deserialise map (_.asInstanceOf[IntegerFact]))
        case ("string list", value) =>
          StringFactList(
            id,
            name,
            safeCastFact[AstList](value).toList map deserialise map (_.asInstanceOf[StringFact]))
        case ("bool list", value) =>
          BooleanFactList(
            id,
            name,
            safeCastFact[AstList](value).toList map deserialise map (_.asInstanceOf[BooleanFact]))
        case (factType, value) => throw DeserialisationException(s"Unknown fact type: $factType with value: $value")
      }
    }
  }

  /**
   * Typeclass instance for serialising story metadata
   */
  implicit object StoryMetadataSerialiser extends Serialisable[Story.Metadata] {
    /**
     * Returns the serialised representation of an object
     *
     * @param t The object to serialise
     */
    override def serialise(t: Metadata): AstElement = AstMap("comments" -> AstString(t.comments),
                                                             "readerStyle" -> ReaderStyleSerialiser.serialise(t.readerStyle),
                                                             "backDisabled" -> AstBoolean(t.isBackButtonDisabled),
                                                             "restartDisabled" -> AstBoolean(t.isRestartButtonDisabled))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Metadata = {
      def safeCastStoryMetadata[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "fact")

      val data = safeCastStoryMetadata[AstMap](serialised)
      val comments = safeCastStoryMetadata[AstString](data("comments")).s
      val readerStyle = ReaderStyleSerialiser deserialise data("readerStyle")
      val backDisabled = safeCastStoryMetadata[AstBoolean](data("backDisabled")).boolean
      val restartDisabled = safeCastStoryMetadata[AstBoolean](data("restartDisabled")).boolean

      Story.Metadata("", "", "",comments, readerStyle, backDisabled, restartDisabled)
    }
  }

  /**
   * Typeclass instance for serialising the reader style information
   */
  implicit object ReaderStyleSerialiser extends Serialisable[ReaderStyle] {
    import org.narrativeandplay.hypedyn.api.story.Narrative.ReaderStyle._

    /**
     * Returns the serialised representation of an object
     *
     * @param t The object to serialise
     */
    override def serialise(t: ReaderStyle): AstElement = t match {
      case Standard => AstString("standard")
      case Fancy => AstString("fancy")
      case Custom(file) => AstString(file)
    }

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): ReaderStyle =
      safeCast[AstString](serialised, "reader style").s match {
        case "standard" => Standard
        case "fancy" => Fancy
        case file => Custom(file)
      }
  }
}
