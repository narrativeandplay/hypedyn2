package org.narrativeandplay.hypedyn.story

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.story.rules.{Fact, RuleLike}

class UiStory(initTitle: String,
              initDescription: String,
              initAuthor: String,
              initFacts: List[Fact],
              initNodes: List[UiNode],
              initRules: List[UiRule]) extends Narrative {
  val titleProperty = StringProperty(initTitle)
  val descriptionProperty = StringProperty(initDescription)
  val authorProperty = StringProperty(initAuthor)

  val factsProperty = ObservableBuffer(initFacts)
  val nodesProperty = ObservableBuffer(initNodes)
  val rulesProperty = ObservableBuffer(initRules)

  /**
   * Returns the title of the story
   */
  override def title: String = titleProperty()

  /**
   * Returns the author of the story
   */
  override def author: String = authorProperty()

  /**
   * Returns the description of the story
   */
  override def description: String = descriptionProperty()

  /**
   * Returns the list of facts of the story
   */
  override def facts: List[Fact] = factsProperty.toList

  /**
   * Returns the nodes contained in the story
   */
  override def nodes: List[UiNode] = nodesProperty.toList

  /**
   * Returns the story-level rules
   */
  override def rules: List[UiRule] = rulesProperty.toList

  def links = nodesProperty flatMap (_.contentProperty().rulesetsProperty) filter { ruleset =>
    ruleset.rules flatMap (_.actions) map (_.actionType) contains "LinkTo"
  }
}
