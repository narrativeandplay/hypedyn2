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
   * Returns the nodes contained in the story
   */
  def nodes: List[Nodal]

  /**
   * Returns the story-level rules
   */
  def rules: List[RuleLike]
}
