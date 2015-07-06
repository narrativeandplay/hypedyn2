package org.narrativeandplay.hypedyn.story

import javafx.beans.property.SimpleMapProperty

import scalafx.Includes._
import scalafx.beans.property.StringProperty

import org.narrativeandplay.hypedyn.story.NodalContent.RulesetIndexes
import org.narrativeandplay.hypedyn.story.rules.RuleLike

class UiNodeContent(initText: String, initRulesets: Map[NodalContent.RulesetIndexes, UiRule]) extends NodalContent {
  val textProperty = StringProperty(initText)
  val rulesetsProperty = new SimpleMapProperty[NodalContent.RulesetIndexes, UiRule]()

  initRulesets foreach { case (k, v) =>
    rulesetsProperty.put(k, v)
  }

  override def text: String = textProperty()

  override def rulesets: Map[RulesetIndexes, RuleLike] = rulesetsProperty.toMap
}

object UiNodeContent {
  def apply(initText: String, initRulesets: Map[NodalContent.RulesetIndexes, UiRule]) =
    new UiNodeContent(initText, initRulesets)
}
