package org.narrativeandplay.hypedyn.story.themes.internal

import org.narrativeandplay.hypedyn.story.themes.{ElementID, MotifLike}

/**
  * Class representing a motif
  *
  * @param id The ID of the motif
  * @param name The name of the motif
  * @param features The features that denote this theme
  */
case class Motif(id: ElementID,
                 name: String,
                 features: List[String]) extends MotifLike
