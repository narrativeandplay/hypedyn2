package org.narrativeandplay.hypedyn.story.themes

/**
  * A motif is a recurring, concrete element of a story denoted by features.
  */
trait MotifLike extends ThematicElement {
  /**
    * Returns the list of features that denote this motif
    */
  def features: List[String]

}
