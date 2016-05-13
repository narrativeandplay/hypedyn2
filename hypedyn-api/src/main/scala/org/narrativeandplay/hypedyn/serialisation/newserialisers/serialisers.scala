package org.narrativeandplay.hypedyn.serialisation.newserialisers

import org.narrativeandplay.hypedyn.serialisation._
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodalContent}
import org.narrativeandplay.hypedyn.story.Narrative.ReaderStyle
import org.narrativeandplay.hypedyn.story.NodalContent.RulesetLike
import org.narrativeandplay.hypedyn.story.rules.BooleanOperator.{And, Or}
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamValue
import org.narrativeandplay.hypedyn.story.rules._

object serialisers {
  /**
   * Typeclass instance for serialising Nodes
   */
  implicit object NodalSerialiser extends Serialisable[Nodal] {
    /**
     * Returns the serialised representation of an object
     *
     * @param node The object to serialise
     */
    override def serialise(node: Nodal): AstElement =
      AstMap(
        "id" -> AstInteger(node.id.value),
        "name" -> AstString(node.name),
        "content" -> NodalContentSerialiser.serialise(node.content),
        "isStart" -> AstBoolean(node.isStartNode),
        "rules" -> AstList(node.rules map RuleLikeSerialiser.serialise: _*)
      )
  }

  /**
   * Implicit class to allow the use of `node.serialise`
   *
   * @param node The node to extend
   */
  implicit class SerialisableNodal(node: Nodal) {
    def serialise = NodalSerialiser.serialise(node)
  }

  /**
   * Typeclass instance for serialising stories
   */
  implicit object StorySerialiser extends Serialisable[Narrative] {
    /**
     * Returns the serialised representation of an object
     *
     * @param story The object to serialise
     */
    override def serialise(story: Narrative): AstElement =
      AstMap(
        "title" -> AstString(story.title),
        "author" -> AstString(story.author),
        "description" -> AstString(story.description),
        "metadata" -> NarrativeMetadataSerialiser.serialise(story.metadata),
        "nodes" -> AstList(story.nodes map NodalSerialiser.serialise: _*),
        "facts" -> AstList(story.facts map FactSerialiser.serialise: _*),
        "rules" -> AstList(story.rules map RuleLikeSerialiser.serialise: _*)
      )
  }

  /**
   * Implicit class to allow `story.serialise`
   *
   * @param story The story to extend
   */
  implicit class SerialisableStory(story: Narrative) {
    def serialise = StorySerialiser.serialise(story)
  }

  /**
   * Typeclass instance for serialising node content
   */
  implicit object NodalContentSerialiser extends Serialisable[NodalContent] {
    private def serialiseSegment(segment: (String, Option[RulesetLike])) =
      AstMap(
        "text" -> AstString(segment._1),
        "ruleset" -> (segment._2 match {
          case Some(ruleset) => RulesetLikeSerialiser serialise ruleset
          case None => AstNull
        })
      )

    /**
     * Returns the serialised representation of an object
     *
     * @param t The object to serialise
     */
    override def serialise(t: NodalContent): AstElement =
      AstList(t.segments map serialiseSegment: _*)
  }

  /**
   * Typeclass instance for serialising rulesets
   */
  implicit object RulesetLikeSerialiser extends Serialisable[NodalContent.RulesetLike] {
    /**
     * Returns the serialised representation of an object
     *
     * @param ruleset The object to serialise
     */
    override def serialise(ruleset: RulesetLike): AstElement =
      AstMap(
        "id" -> AstInteger(ruleset.id.value),
        "name" -> AstString(ruleset.name),
        "start" -> AstInteger(ruleset.indexes.startIndex.index),
        "end" -> AstInteger(ruleset.indexes.endIndex.index),
        "rules" -> AstList(ruleset.rules map RuleLikeSerialiser.serialise: _*)
      )
  }

  /**
   * Typeclass instance for serialising rules
   */
  implicit object RuleLikeSerialiser extends Serialisable[RuleLike] {
    /**
     * Returns the serialised representation of an object
     *
     * @param rule The object to serialise
     */
    override def serialise(rule: RuleLike): AstElement =
      AstMap(
        "id" -> AstInteger(rule.id.value),
        "name" -> AstString(rule.name),
        "stopIfTrue" -> AstBoolean(rule.stopIfTrue),
        "conditionsOp" -> AstString(rule.conditionsOp match {
                                      case And => "and"
                                      case Or => "or"
                                    }),
        "conditions" -> AstList(rule.conditions map ConditionalSerialiser.serialise: _*),
        "actions" -> AstList(rule.actions map ActionableSerialiser.serialise: _*)
      )
  }

  private def paramValueToAstMap(paramValue: ParamValue): AstMap =  {
    import ParamValue._
    paramValue match {
      case ParamValue.Node(n) =>
        AstMap(
          "type" -> AstString("node"),
          "value" -> AstInteger(n.value)
        )

      case Link(l) =>
        AstMap(
          "type" -> AstString("link"),
          "value" -> AstInteger(l.value)
        )

      case IntegerFact(f) =>
        AstMap(
          "type" -> AstString("integerFact"),
          "value" -> AstInteger(f.value)
        )

      case BooleanFact(f) =>
        AstMap(
          "type" -> AstString("booleanFact"),
          "value" -> AstInteger(f.value)
        )

      case StringFact(f) =>
        AstMap(
          "type" -> AstString("stringFact"),
          "value" -> AstInteger(f.value)
        )

      case StringInput(s) =>
        AstMap(
          "type" -> AstString("string"),
          "value" -> AstString(s)
        )

      case IntegerInput(i) =>
        AstMap(
          "type" -> AstString("integer"),
          "value" -> AstInteger(i)
        )

      case SelectedListValue(s) =>
        AstMap(
          "type" -> AstString("selectedListValue"),
          "value" -> AstString(s)
        )

      case UnionValueSelected(p) =>
        AstMap(
          "type" -> AstString("union"),
          "value" -> AstString(p)
        )

      case ProductValue(ns) =>
        AstMap(
          "type" -> AstString("product"),
          "value" -> AstString(ns mkString ":")
        )
    }
  }

  /**
   * Typeclass instance for serialsing conditions
   */
  implicit object ConditionalSerialiser extends Serialisable[Conditional] {
    /**
     * Returns the serialised representation of an object
     *
     * @param condition The object to serialise
     */
    override def serialise(condition: Conditional): AstElement =
      AstMap(
        "conditionType" -> AstString(condition.conditionType.value),
        "params" -> AstMap((condition.params map { case (k, v) =>
          k.value -> paramValueToAstMap(v)
        }).toSeq: _*)
      )
  }

  /**
   * Typeclass instance for serialising actions
   */
  implicit object ActionableSerialiser extends Serialisable[Actionable] {
    /**
     * Returns the serialised representation of an object
     *
     * @param action The object to serialise
     */
    override def serialise(action: Actionable): AstElement =
      AstMap(
        "actionType" -> AstString(action.actionType.value),
        "params" -> AstMap((action.params map { case (k, v) =>
          k.value -> paramValueToAstMap(v)
        }).toSeq: _*)
      )
  }

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

      AstMap(
        "id" -> AstInteger(fact.id.value),
        "name" -> AstString(fact.name),
        "type" -> AstString(factType),
        "initialValue" -> value
      )
    }
  }

  /**
   * Typeclass instance for serialising story metadata
   */
  implicit object NarrativeMetadataSerialiser extends Serialisable[Narrative.Metadata] {
    /**
     * Returns the serialised representation of an object
     *
     * @param t The object to serialise
     */
    override def serialise(t: Narrative.Metadata): AstElement =
      AstMap(
        "comments" -> AstString(t.comments),
        "readerStyle" -> ReaderStyleSerialiser.serialise(t.readerStyle),
        "backDisabled" -> AstBoolean(t.isBackButtonDisabled),
        "restartDisabled" -> AstBoolean(t.isRestartButtonDisabled)
      )
  }

  /**
   * Typeclass instance for serialising the reader style information
   */
  implicit object ReaderStyleSerialiser extends Serialisable[ReaderStyle] {

    import Narrative.ReaderStyle._

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
  }
}
