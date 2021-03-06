package org.narrativeandplay.hypedyn.ui.story

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.api.story.NodalContent
import org.narrativeandplay.hypedyn.api.story.NodalContent.{RulesetIndexes, RulesetLike}
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike
import org.narrativeandplay.hypedyn.api.utils.PrettyPrintable

/**
 * UI implementation of NodalContent
 *
 * @param initText The initial text of the node
 * @param initRulesets The initial list of text rules for the node
 */
class UiNodeContent(initText: String, initRulesets: List[UiNodeContent.UiRuleset])
  extends NodalContent
  with PrettyPrintable {
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

  override def toString: String = {
    val fields = List("text" -> text, "rulesets" -> rulesets)
    val doc = list(fields, getClass.getSimpleName, any)
    pretty(doc).layout
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
                  initRules: List[UiRule]) extends NodalContent.RulesetLike with PrettyPrintable {
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
      val fields = List("id" -> id, "name" -> name, "indexes" -> indexes, "rules" -> rules)
      val doc = list(fields, getClass.getSimpleName, any)
      pretty(doc).layout
    }
  }
}
