package org.narrativeandplay.hypedyn.story.themes.internal

import org.narrativeandplay.hypedyn.story.themes.{ElementID, ThemeLike}

/**
  * Class representing a theme
  *
  * @param id The ID of the theme
  * @param name The name of the theme
  * @param subthemes The subthemes that connote this theme
  * @param motifs The motifs that connote this theme
  */
case class Theme(id: ElementID,
                name: String,
                subthemes: List[ElementID],
                motifs: List[ElementID]) extends ThemeLike
