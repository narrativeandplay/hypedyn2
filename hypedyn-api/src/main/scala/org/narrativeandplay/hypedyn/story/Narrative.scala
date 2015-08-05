package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.{Fact, RuleLike}

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
}

object Narrative {
  trait Metadata {
    def comments: String

    def readerStyle: ReaderStyle
    def isBackButtonDisabled: Boolean
    def isRestartButtonDisabled: Boolean
  }

  sealed trait ReaderStyle
  object ReaderStyle {
    case object Standard extends ReaderStyle
    case object Fancy extends ReaderStyle
    case class Custom(cssFilePath: String) extends ReaderStyle
  }
}
