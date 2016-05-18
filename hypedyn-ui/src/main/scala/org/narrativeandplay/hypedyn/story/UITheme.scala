package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.themes.{ThematicElementID, ThemeLike}

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

/**
  * UI implementation for MotifLike
  */
class UITheme(initId: ThematicElementID,
              initName: String,
              initSubthemes: List[ThematicElementID],
              initMotifs: List[ThematicElementID]) extends ThemeLike {

  /**
    * Backing property for the name
    */
  val nameProperty = StringProperty(initName)

  /**
    * Backing property for the id
    */
  val idProperty = ObjectProperty(initId)

  /**
    * Backing property for the list of subthemes
    */
  val subthemesProperty = ObjectProperty(ObservableBuffer(initSubthemes: _*))

  /**
    * Backing property for the list of motifs
    */
  val motifsProperty = ObjectProperty(ObservableBuffer(initMotifs: _*))

  /**
    * The name of the theme
    */
  override def name: String = nameProperty()

  /**
    * The id of the theme
    */
  override def id: ThematicElementID = idProperty()

  /**
    * The list of subthemes of the theme
    */
  override def subthemes: List[ThematicElementID] = subthemesProperty().toList

  /**
    * The list of motifs of the theme
    */
  override def motifs: List[ThematicElementID] = motifsProperty().toList

  override def toString: String = {
    val subthemesString = subthemes match {
      case Nil => "Nil"
      case _ => s"""List(
                    |    ${subthemes map (_.toString) mkString ",\n        "})"""
    }
    val motifsString = subthemes match {
      case Nil => "Nil"
      case _ => s"""List(
                    |    ${motifs map (_.toString) mkString ",\n        "})"""
    }
    s"""UiTheme(
        |  id = $id,
        |  name = $name,
        |  subthemes = $subthemesString,
        |  motifs = $motifsString""".stripMargin
  }

  def copy = new UITheme(id, name, subthemes, motifs)
}

object UITheme {
  def apply(id: ThematicElementID,
            initName: String,
            initSubthemes: List[ThematicElementID],
            initMotifs: List[ThematicElementID]) = new UITheme(id, initName, initSubthemes, initMotifs)
}
