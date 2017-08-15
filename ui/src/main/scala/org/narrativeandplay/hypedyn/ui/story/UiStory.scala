package org.narrativeandplay.hypedyn.ui.story

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.api.story.Narrative
import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.api.story.Narrative.{Metadata, ReaderStyle}
import org.narrativeandplay.hypedyn.api.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.core.story.rules.ActionDefinitions

/**
 * UI implementation for Narrative
 *
 * @param initMetadata The initial metadata of the story
 * @param initFacts The initial facts of the story
 * @param initNodes The initial list of nodes of the story
 * @param initRules The initial list of story-level rules
 */
class UiStory(initMetadata: UiStory.UiStoryMetadata,
              initFacts: List[Fact],
              initNodes: List[UiNode],
              initRules: List[UiRule]) extends Narrative {
  /**
   * Backing property for the metadata
   */
  val metadataProperty = ObjectProperty(initMetadata)

  /**
   * Backing property for the list of facts
   */
  val factsProperty = ObjectProperty(ObservableBuffer(initFacts))

  /**
   * Backing propety for the list of nodes
   */
  val nodesProperty = ObjectProperty(ObservableBuffer(initNodes))

  /**
   * Backing property for the list of story-level rules
   */
  val rulesProperty = ObjectProperty(ObservableBuffer(initRules))

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

  /**
   * Returns an observable buffer (why not a list?) of rulesets that can be activated (usually by clicking)
   */
  def canActivate: ObservableBuffer[UiNodeContent.UiRuleset] =
    nodesProperty() flatMap (_.contentProperty().rulesetsProperty()) filter { ruleset =>
      ActionDefinitions() filter (_.canActivate) map (_.actionType.value) exists { thisActionType =>
        ruleset.rulesProperty.exists( rule => rule.actions.map(_.actionType) contains ActionType(thisActionType))
    }
  }
}

object UiStory {

  /**
   * UI implementation for Narrative.Metadata
   *
   * @param initTitle The initial title of the story
   * @param initDescription The initial description of the story
   * @param initAuthor The initial author of the story
   * @param initComments The initial comments of the story
   * @param initReaderStyle The initial style of the reader
   * @param initBackDisabled The initial value of whether the back button is disabled
   * @param initRestartDisabled The initial value of whether the restart button is disabled
   */
  class UiStoryMetadata(initTitle: String,
                        initAuthor: String,
                        initDescription: String,
                        initComments: String,
                        initReaderStyle: ReaderStyle,
                        initBackDisabled: Boolean,
                        initRestartDisabled: Boolean) extends Narrative.Metadata {
    /**
     * Backing property for the title
     */
    val titleProperty = StringProperty(initTitle)

    /**
     * Backing property for the description
     */
    val descriptionProperty = StringProperty(initDescription)

    /**
     * Backing property for the author
     */
    val authorProperty = StringProperty(initAuthor)

    /**
     * Backing property for the comments
     */
    val commentsProperty = StringProperty(initComments)

    /**
     * Backing property for the reader style
     */
    val readerStyleProperty = ObjectProperty(initReaderStyle)

    /**
     * Backing property for whether the back button is disabled
     */
    val backDisabledProperty = BooleanProperty(initBackDisabled)

    /**
     * Backing property for whether the restart button is disabled
     */
    val restartDisabledProperty = BooleanProperty(initRestartDisabled)

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
     * Returns the story comments
     */
    override def comments: String = commentsProperty()

    /**
     * Returns true if the restart button is to be disabled
     */
    override def isRestartButtonDisabled: Boolean = restartDisabledProperty()

    /**
     * Returns true if the back button is to be disabled
     */
    override def isBackButtonDisabled: Boolean = backDisabledProperty()

    /**
     * Returns the style of the reader
     */
    override def readerStyle: ReaderStyle = readerStyleProperty()

    override def toString: String =
      s"""${getClass.getSimpleName} (
         |  title: "$title",
         |  author: "$author",
         |  description: "$description",
         |  comments: "$comments",
         |  readerStyle: $readerStyle,
         |  backDisabled?: $isBackButtonDisabled,
         |  restartDisabled: $isRestartButtonDisabled)""".stripMargin
  }
}
