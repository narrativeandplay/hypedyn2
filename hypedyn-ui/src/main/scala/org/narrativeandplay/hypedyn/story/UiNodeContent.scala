package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.{ObservableBuffer, ObservableMap}

import org.narrativeandplay.hypedyn.story.NodalContent.{RulesetLike, RulesetIndexes}
import org.narrativeandplay.hypedyn.story.rules.RuleLike

class UiNodeContent(initText: String, initRulesets: List[UiNodeContent.UiRuleset]) extends NodalContent {
  val textProperty = StringProperty(initText)
  val rulesetsProperty = ObservableBuffer(initRulesets)

  override def text: String = textProperty()

  override def rulesets: List[RulesetLike] = rulesetsProperty.toList

  override def toString: String = s"${getClass.getCanonicalName}(text = '$text', rulesets = $rulesets"
}

object UiNodeContent {
  def apply(initText: String, initRulesets: List[UiRuleset]) =
    new UiNodeContent(initText, initRulesets)

  class UiRuleset(val id: NodalContent.RulesetId,
                  initName: String,
                  initIndexes: RulesetIndexes,
                  initRules: List[UiRule]) extends NodalContent.RulesetLike {
    val nameProperty = StringProperty(initName)
    val indexesProperty = ObjectProperty(initIndexes)
    val rulesProperty = ObservableBuffer(initRules)

    override def name: String = nameProperty()

    override def rules: List[RuleLike] = rulesProperty.toList

    override def indexes: RulesetIndexes = indexesProperty()

    override def hashCode(): Int = id.hashCode()

    override def equals(that: Any): Boolean = that match {
      case that: UiRuleset => (that canEqual this) && (id == that.id)
      case _ => false
    }

    def canEqual(that: Any): Boolean = that.isInstanceOf[UiRuleset]

    override def toString: String = s"${getClass.getCanonicalName}(id = $id, name = $name, indexes = (${indexes.startIndex.index}, ${indexes.endIndex.index}) rules = $rules)"
  }
}
