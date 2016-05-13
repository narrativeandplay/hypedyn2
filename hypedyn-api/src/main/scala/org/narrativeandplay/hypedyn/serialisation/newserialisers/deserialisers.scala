package org.narrativeandplay.hypedyn.serialisation.newserialisers

import scala.reflect.ClassTag

import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodalContent, NodeId}
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle.{Custom, Fancy, Standard}
import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetId, RulesetLike}
import org.narrativeandplay.hypedyn.story.rules.BooleanOperator.{And, Or}
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamValue
import org.narrativeandplay.hypedyn.story.rules._

object deserialisers {
  private case class StoryMetadata(comments: String,
                                   readerStyle: ReaderStyle,
                                   isBackButtonDisabled: Boolean,
                                   isRestartButtonDisabled: Boolean) extends Narrative.Metadata
  private case class Action(actionType: Actionable.ActionType,
                            params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Actionable
  private case class Condition(conditionType: Conditional.ConditionType,
                               params: Map[RuleLike.ParamName, RuleLike.ParamValue]) extends Conditional
  private case class Rule(id: RuleId,
                          name: String,
                          stopIfTrue: Boolean,
                          conditionsOp: BooleanOperator,
                          conditions: List[Conditional],
                          actions: List[Actionable]) extends RuleLike
  private case class Ruleset(id: NodalContent.RulesetId,
                             name: String,
                             indexes: NodalContent.RulesetIndexes,
                             rules: List[RuleLike]) extends NodalContent.RulesetLike
  private case class Node(id: NodeId,
                          name: String,
                          content: NodalContent,
                          isStartNode: Boolean,
                          rules: List[RuleLike]) extends Nodal
  private case class NodeContent(segments: List[(String, Option[NodalContent.RulesetLike])]) extends NodalContent {
    override def text: String = segments map (_._1) mkString ""
    override def rulesets: List[NodalContent.RulesetLike] = segments flatMap (_._2)
  }
  private case class Story(title: String,
                           author: String,
                           description: String,
                           metadata: Narrative.Metadata,
                           nodes: List[Nodal],
                           facts: List[Fact],
                           rules: List[RuleLike]) extends Narrative

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

  implicit object ReaderStyleDeserialiser extends Deserialisable[ReaderStyle] {
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

  implicit object NarrativeMetadataDeserialiser extends Deserialisable[Narrative.Metadata] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Narrative.Metadata = {
      def safeCastStoryMetadata[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "fact")

      val data = safeCastStoryMetadata[AstMap](serialised)
      val comments = safeCastStoryMetadata[AstString](data("comments")).s
      val readerStyle = ReaderStyleDeserialiser deserialise data("readerStyle")
      val backDisabled = safeCastStoryMetadata[AstBoolean](data("backDisabled")).boolean
      val restartDisabled = safeCastStoryMetadata[AstBoolean](data("restartDisabled")).boolean

      StoryMetadata(comments, readerStyle, backDisabled, restartDisabled)
    }
  }

  implicit object FactDeserialiser extends Deserialisable[Fact] {
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

  implicit object ActionableDeserialiser extends Deserialisable[Actionable] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Actionable = {
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

  implicit object ConditionalDeserialiser extends Deserialisable[Conditional] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Conditional = {
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

  implicit object RuleLikeDeserialiser extends Deserialisable[RuleLike] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): RuleLike = {
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
      val conditions = safeCastRule[AstList](data("conditions")).toList map ConditionalDeserialiser.deserialise
      val actions = safeCastRule[AstList](data("actions")).toList map ActionableDeserialiser.deserialise

      Rule(id, name, stopIfTrue, conditionsOp, conditions, actions)
    }
  }

  implicit object RulesetLikeDeserialiser extends Deserialisable[RulesetLike] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): RulesetLike = {
      def safeCastRuleset[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "ruleset")

      import NodalContent._
      val data = safeCastRuleset[AstMap](serialised)
      val id = RulesetId(safeCastRuleset[AstInteger](data("id")).i)
      val name = safeCastRuleset[AstString](data("name")).s
      val indexes = RulesetIndexes(TextIndex(safeCastRuleset[AstInteger](data("start")).i),
                                   TextIndex(safeCastRuleset[AstInteger](data("end")).i))
      val rules = safeCastRuleset[AstList](data("rules")).toList map RuleLikeDeserialiser.deserialise

      Ruleset(id, name, indexes, rules)
    }
  }

  implicit object NodalContentDeserialiser extends Deserialisable[NodalContent] {
    private def deserialiseSegment(serialised: AstElement) = {
      def safeCastSegment[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "segment")

      val segment = safeCastSegment[AstMap](serialised).toMap

      // Sanity check
      assert(segment.size == 2)
      assert(segment contains "text")
      assert(segment contains "ruleset")

      val text = safeCastSegment[AstString](segment("text")).s
      val rulesetOption = segment("ruleset") match {
        case r: AstMap => Some(RulesetLikeDeserialiser deserialise r)
        case AstNull => None
        case unknown => throw DeserialisationException(s"Unexpected AST type in deserialising ruleset: $unknown")
      }

      (text, rulesetOption)
    }

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): NodalContent = {
      def safeCastSegment[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) =
        safeCast[T](astElement, "node content")

      val data = safeCastSegment[AstList](serialised).toList
      val content = data map deserialiseSegment

      NodeContent(content)
    }
  }

  implicit object NarrativeDeserialiser extends Deserialisable[Narrative] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Narrative = {
      def safeCastStory[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) = safeCast[T](astElement, "story")

      val data = safeCastStory[AstMap](serialised)
      val title = safeCastStory[AstString](data("title")).s
      val author = safeCastStory[AstString](data("author")).s
      val description = safeCastStory[AstString](data("description")).s
      val metadata = NarrativeMetadataDeserialiser.deserialise(data("metadata"))
      val nodes = safeCastStory[AstList](data("nodes")).toList map NodalDeserialiser.deserialise
      val facts = safeCastStory[AstList](data("facts")).toList map FactDeserialiser.deserialise
      val rules = safeCastStory[AstList](data("rules")).toList map RuleLikeDeserialiser.deserialise

      Story(title, author, description, metadata, nodes, facts, rules)
    }
  }

  implicit object NodalDeserialiser extends Deserialisable[Nodal] {
    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Nodal = {
      def safeCastNode[T <: AstElement](astElement: AstElement)(implicit ev: ClassTag[T]) = safeCast[T](astElement, "node")

      val data = safeCastNode[AstMap](serialised)
      val id = safeCastNode[AstInteger](data("id")).i
      val name = safeCastNode[AstString](data("name")).s
      val content = NodalContentDeserialiser.deserialise(data("content"))
      val isStart = safeCastNode[AstBoolean](data("isStart")).boolean
      val rules = safeCastNode[AstList](data("rules")).toList map RuleLikeDeserialiser.deserialise

      Node(NodeId(id), name, content, isStart, rules)
    }
  }
}
