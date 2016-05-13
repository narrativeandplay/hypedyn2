package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.{ObservableBuffer, ObservableMap}

import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetLike, RulesetIndexes}
import org.narrativeandplay.hypedyn.story.rules.RuleLike

/**
 * UI implementation of NodalContent
 *
 * @param initText The initial text of the node
 * @param initRulesets The initial list of text rules for the node
 */
class UiNodeContent(initText: String, initRulesets: List[UiNodeContent.UiRuleset]) extends NodalContent {
  /**
   * Backing property for the node text
   */
  val textProperty = StringProperty(initText)

  /**
   * Backing property for the text rules
   */
  val rulesetsProperty = ObjectProperty(ObservableBuffer(initRulesets))

  /**
   * Returns the text of the node
   */
  override def text: String = textProperty()

  /**
   * Returns the list of text rules of the node
   */
  override def rulesets: List[RulesetLike] = rulesetsProperty().toList


  /**
   * Returns the list of segments of the node
   */
  override def segments: List[(String, Option[RulesetLike])] =  {
    // See http://stackoverflow.com/questions/37091135/inserting-missing-values-into-a-list/37092356#37092356
    val triples = rulesets map { ruleset =>
      (ruleset.indexes.startIndex.index, ruleset.indexes.endIndex.index, Option(ruleset))
    } sortBy (_._1)
    val xs = (BigInt(0), BigInt(0), None) :: (triples :+ ((BigInt(text.length), BigInt(text.length), None)))

    val ys = xs sliding 2 flatMap { case List(fst @ (_, end1, _), (start2, _, _)) =>
      if (end1 != start2) List(fst, (end1, start2, None)) else List(fst)
    }
    val filledInRulesets = ys.toList.tail

    filledInRulesets map { case (start, end, rulesetOption) =>
      (text substring (start.intValue(), end.intValue() + 1), rulesetOption)
    }
  }

  override def toString: String = {
    val rulesetsString = rulesets match {
      case Nil => "Nil"
      case _ => s"""List(
                   |      ${rulesets map (_.toString) mkString ",\n        "})"""
    }
    s"""${getClass.getSimpleName} (
       |    text = "$text",
       |    rulesets = $rulesetsString""".stripMargin
  }
}

object UiNodeContent {
  def apply(initText: String, initRulesets: List[UiRuleset]) =
    new UiNodeContent(initText, initRulesets)

  /**
   * UI imlementation for RulesetLike
   *
   * @param id The ID of the ruleset
   * @param initName The initial name of the ruleset
   * @param initIndexes The initial index range of where the ruleset is in the text
   * @param initRules The initial list of rules of the ruleset
   */
  class UiRuleset(val id: NodalContent.RulesetId,
                  initName: String,
                  initIndexes: RulesetIndexes,
                  initRules: List[UiRule]) extends NodalContent.RulesetLike {
    /**
     * Backing property for the name
     */
    val nameProperty = StringProperty(initName)

    /**
     * Backing property for the indexes
     */
    val indexesProperty = ObjectProperty(initIndexes)

    /**
     * Backing property for the list of rules
     */
    val rulesProperty = ObservableBuffer(initRules)

    /**
     * Returns the name of the text rule
     */
    override def name: String = nameProperty()

    /**
     * Returns the list of rules that are in this text rule
     */
    override def rules: List[RuleLike] = rulesProperty.toList

    /**
     * Returns the index range in the rule text where this text rule applies
     */
    override def indexes: RulesetIndexes = indexesProperty()

    override def hashCode(): Int = id.hashCode()

    override def equals(that: Any): Boolean = that match {
      case that: UiRuleset => (that canEqual this) && (id == that.id)
      case _ => false
    }

    def canEqual(that: Any): Boolean = that.isInstanceOf[UiRuleset]

    override def toString: String = {
      val rulesString = rules match {
        case Nil => "Nil"
        case _ => s"""List(
                     |    ${rules map (_.toString) mkString ",\n        "})"""
      }
      s"""${getClass.getSimpleName}(
         |      id = $id,
         |      name = $name,
         |      indexes = (${indexes.startIndex.index}, ${indexes.endIndex.index}),
         |      rules = $rulesString""".stripMargin
    }
  }
}
