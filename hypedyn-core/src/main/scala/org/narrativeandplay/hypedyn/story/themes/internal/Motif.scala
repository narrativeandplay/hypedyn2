package org.narrativeandplay.hypedyn.story.themes.internal

import org.narrativeandplay.hypedyn.story.themes.{ThematicElementID, MotifLike}

/**
  * Class representing a motif
  *
  * @param id The ID of the motif
  * @param name The name of the motif
  * @param features The features that denote this theme
  */
case class Motif(id: ThematicElementID,
                 name: String,
                 features: List[String]) extends MotifLike
