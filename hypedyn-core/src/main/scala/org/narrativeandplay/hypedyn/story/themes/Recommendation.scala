package org.narrativeandplay.hypedyn.story.themes

import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.{Node, Story}
import org.narrativeandplay.hypedyn.story.themes.internal.{Motif, Theme}

import scala.xml.NodeSeq

/**
  * Calculates a thematic recommendation.
  */
object Recommendation {
  /**
    * Given a text string and an (optional) threshould, recommends a list of nodes from the story that contain the
    * themes found in the string
    *
    * @param text string to match against
    * @param threshold the cutoff point for themes
    * @return list of ordered pairs consisting of IDs of nodes that match the themes in text, and their scores
    */
  def recommendation(text: String, threshold: Double = StoryController.story.metadata.themeThreshold) : List[(Node, Double)] = {
    recommendation(containedThemes(text, StoryController.story.themes, StoryController.story), threshold)
  }

  /**
    * Given a list of themes and a threshold, recommends a list of IDs of nodes from the story that contain the themes
    *
    * @param sourceThemes list of themes to match
    * @param threshold the cutoff point for themes
    * @return list of ordered pairs consisting of nodes that match sourceThemes, and their scores
    */
  def recommendation(sourceThemes: List[Theme], threshold: Double): List[(Node, Double)] = {
    StoryController.story.nodes map{node =>
      Tuple2(node,
        (thematicCoverage(sourceThemes, node.content.text, StoryController.story) +
        componentCoverage(sourceThemes, node.content.text, StoryController.story))/2.0)
    } filter(_._2 > threshold) sortWith(_._2 > _._2)
  }

  /**
    * Calculates the thematic coverage: contained themes/total themes
    *
    * @param themes themes to check for
    * @param text text to check
    * @param story the story
    * @return thematic coverage score
    */
  def thematicCoverage(themes: List[Theme], text: String, story: Story): Double = {
    themes.length match {
      case 0 => 0.0
      case _ => containedThemes(text, themes, story).length.toDouble/themes.length.toDouble
    }
  }

  /**
    * Calculates the component coverage: (motifs covered + subtheme coverage)/(motifs + subthemes)
    * Note: for this, the motifs covered count isn't recursive
    *
    * @param themes themes to check for
    * @param text text to check
    * @param story the story
    * @return the component coverage score
    */
  def componentCoverage(themes: List[Theme], text: String, story: Story): Double = {
    val allMotifs = getAllMotifs(themes, story, false)
    val allSubthemes = getAllSubthemes(themes, story)
    (motifsCoveredCount(text, allMotifs)+thematicCoverage(allSubthemes, text, story)*allSubthemes.length)/(allMotifs.length+allSubthemes.length)
  }

  /**
    * Returns a list of themes contained in the given text
    *
    * @param text target text
    * @param allThemes list of themes
    * @param story the story
    * @return list of themes
    */
  def containedThemes(text: String, allThemes: List[Theme], story: Story): List[Theme] = {
    allThemes filter(theme =>
      motifsAreCovered(text, getAllMotifs(theme, story, true)))
  }

  /**
    * Helper function to get all motifs in a list of themes;
    * if recursive is true, then will recursively get motifs for all the subthemes as well
    *
    * @param themes themes from which to retrieve the motifs
    * @param story the story
    * @param recursive should the motifs be retrieved from subthemes as well? (default is true)
    * @return list of all motifs
    */
  private def getAllMotifs(themes: List[Theme], story: Story, recursive: Boolean): List[Motif] = {
    themes flatMap { theme =>
      getAllMotifs(theme, story, recursive)
    }
  }

  /**
    * Helper function to get all motifs in a single theme;
    * if recursive is true, then will recursively get motifs for all the subthemes as well
    *
    * @param theme theme from which to retrieve the motifs
    * @param story the story
    * @param recursive should the motifs be retrieved from subthemes as well? (default is true)
    * @return list of all motifs
    */
  private def getAllMotifs(theme: Theme, story: Story, recursive: Boolean = false): List[Motif] = {
    List(
      theme.motifs flatMap (motifID =>
        story.motifs.find(_.id == motifID)),
      (recursive, theme.subthemes.nonEmpty) match {
        case (true, true) =>
          getAllMotifs(getAllSubthemes(theme, story), story, recursive)
        case _ => List[Motif]()
      }).flatten
  }


  /**
    * Helper function to get all subthemes in a list of themes
    *
    * @param themes themes from which to get subthemes
    * @param story the story
    * @return list of all subthemes
    */
  private def getAllSubthemes(themes: List[Theme], story: Story): List[Theme] = {
    themes flatMap{theme =>
      getAllSubthemes(theme, story)
    }
  }

  /**
    * Helper function to get all subthemes in a theme
    *
    * @param theme theme from which to get subthemes
    * @param story the story
    * @return list of all subthemes
    */
  private def getAllSubthemes(theme:Theme, story: Story): List[Theme] = {
    theme.subthemes flatMap{motifID =>
      story.themes.find(_.id == motifID)
    }
  }

  /**
    * checks whether given motifs are contained in the given text (ie. any of the motifs' features appear in the text)
    *
    * @param text the text to check
    * @param motifs the list of motifs
    * @return true if the motifs are covered
    */
  def motifsAreCovered(text: String, motifs: List[Motif]): Boolean = {
    motifsCoveredCount(text, motifs) > 0
  }

  /**
    * counts how many motifs are covered in the given text
    *
    * @param text the text to check
    * @param motifs the list of motifs
    * @return number of motifs covered
    */
  def motifsCoveredCount(text: String, motifs: List[Motif]): Int = {
    motifs map(motif =>
      motif.features exists(feature =>
        stringContainsPhrase(text.replaceAll("\\r\\n|\\r|\\n", " "), feature.replaceAll("\\r\\n|\\r|\\n", " "))
      )
    ) count(_ == true)
  }

  /**
    * Checks if a string contains a phrase
    *
    * @param string string to check
    * @param phrase phrase to check for
    * @return true if the string contains the phrase
    */
  def stringContainsPhrase(string: String, phrase: String): Boolean = {
    string.toLowerCase.trim.indexOf(phrase.toLowerCase.trim) match {
      case -1 => false
      case theIndex =>
        ((theIndex==0)||delimiterCheck(string.substring(theIndex-1, theIndex)))&&
          ((theIndex+phrase.length==string.length)||delimiterCheck(string.substring(theIndex+phrase.length, theIndex+phrase.length+1)))
    }
  }

  private def delimiterCheck(string: String): Boolean = {
    val delimiters = List(" ",".",",")

    delimiters.exists(string.contains)
  }
}
