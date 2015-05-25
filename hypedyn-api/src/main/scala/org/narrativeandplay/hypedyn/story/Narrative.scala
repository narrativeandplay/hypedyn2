package org.narrativeandplay.hypedyn.story

trait Narrative extends NarrativeElement {
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
}
