package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.{Fact, RuleLike}
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThemeLike}

/**
 * An interface for a story
 */
trait Narrative extends NarrativeElement[Narrative] {
  /**
   * Returns the title of the story
   */
  def title: String

  /**
   * Returns the author of the story
   */
  def author: String

  /**
   * Returns the description of the story
   */
  def description: String

  /**
   * Returns the metadata of the story
   */
  def metadata: Narrative.Metadata

  /**
   * Returns the nodes contained in the story
   */
  def nodes: List[Nodal]

  /**
   * Returns the list of facts of the story
   */
  def facts: List[Fact]

  /**
   * Returns the story-level rules
   */
  def rules: List[RuleLike]

  /**
    * Returns the themes contained in the story
    */
  def themes: List[ThemeLike]

  /**
    * Returns the motifs contained in the story
    */
  def motifs: List[MotifLike]
}

object Narrative {

  /**
   * An interface for story metadata, which includes comments about the story,
   * the style of the reader, and whether the back and/or restart buttons are
   * disabled
   */
  trait Metadata {
    /**
     * Returns the story comments
     */
    def comments: String

    /**
     * Returns the style of the reader
     */
    def readerStyle: ReaderStyle

    /**
     * Returns true if the back button is to be disabled
     */
    def isBackButtonDisabled: Boolean

    /**
     * Returns true if the restart button is to be disabled
     */
    def isRestartButtonDisabled: Boolean

    /**
      * Returns the threshold for thematic links
      */
    def themeThreshold: Double
  }

  /**
   * Enumeration for the available reader styles
   */
  sealed trait ReaderStyle
  object ReaderStyle {

    /**
     * The standard reader style
     */
    case object Standard extends ReaderStyle

    /**
     * A fancy reader style
     */
    case object Fancy extends ReaderStyle

    /**
     * A custom reader style
      *
      * @param cssFilePath The absolute file path to the custom stylesheet
     */
    case class Custom(cssFilePath: String) extends ReaderStyle
  }
}
