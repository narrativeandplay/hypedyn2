package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.story.Narrative.{Metadata, ReaderStyle}
import org.narrativeandplay.hypedyn.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.story.rules.Fact

class UiStory(initTitle: String,
              initDescription: String,
              initAuthor: String,
              initMetadata: UiStory.UiStoryMetadata,
              initFacts: List[Fact],
              initNodes: List[UiNode],
              initRules: List[UiRule]) extends Narrative {
  val titleProperty = StringProperty(initTitle)
  val descriptionProperty = StringProperty(initDescription)
  val authorProperty = StringProperty(initAuthor)
  val metadataProperty = ObjectProperty(initMetadata)

  val factsProperty = ObjectProperty(ObservableBuffer(initFacts))
  val nodesProperty = ObjectProperty(ObservableBuffer(initNodes))
  val rulesProperty = ObjectProperty(ObservableBuffer(initRules))

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
   * Returns the metadata of the story
   */
  override def metadata: UiStory.UiStoryMetadata = metadataProperty()

  /**
   * Returns the list of facts of the story
   */
  override def facts: List[Fact] = factsProperty().toList

  /**
   * Returns the nodes contained in the story
   */
  override def nodes: List[UiNode] = nodesProperty().toList

  /**
   * Returns the story-level rules
   */
  override def rules: List[UiRule] = rulesProperty().toList

  def links: ObservableBuffer[UiRule] = nodesProperty() flatMap (_.contentProperty().rulesetsProperty()) flatMap (_.rulesProperty) filter { rule =>
    rule.actions map (_.actionType) contains ActionType("LinkTo")
  }
}

object UiStory {
  class UiStoryMetadata(initComments: String,
                        initReaderStyle: ReaderStyle,
                        initBackDisabled: Boolean,
                        initRestartDisabled: Boolean) extends Narrative.Metadata {
    val commentsProperty = StringProperty(initComments)
    val readerStyleProperty = ObjectProperty(initReaderStyle)
    val backDisabledProperty = BooleanProperty(initBackDisabled)
    val restartDisabledProperty = BooleanProperty(initRestartDisabled)

    override def comments: String = commentsProperty()

    override def isRestartButtonDisabled: Boolean = restartDisabledProperty()

    override def isBackButtonDisabled: Boolean = backDisabledProperty()

    override def readerStyle: ReaderStyle = readerStyleProperty()

    override def toString: String = s"${getClass.getSimpleName}(comments: $comments, readerStyle: $readerStyle, backDisabled?: $isBackButtonDisabled, restartDisabled: $isRestartButtonDisabled)"
  }
}
