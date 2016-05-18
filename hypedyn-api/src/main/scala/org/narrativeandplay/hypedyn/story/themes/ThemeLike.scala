package org.narrativeandplay.hypedyn.story.themes

/**
  * Themes are abstract concepts in a story connoted by other themes and by motifs in a particular context.
  */
trait ThemeLike extends ThematicElement[ThemeLike] {
  /**
    * Returns the list of IDs of themes that connote this theme
    */
  def subthemes: List[ThematicElementID]

  /**
    * Returns the list of IDs of motifs that connote this theme
    */
  def motifs: List[ThematicElementID]
}
