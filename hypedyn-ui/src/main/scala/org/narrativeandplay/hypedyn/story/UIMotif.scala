package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID}

import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

/**
  * UI implementation for MotifLike
  */
class UIMotif(initId: ThematicElementID,
              initName: String,
              initFeatures: List[String]) extends MotifLike {

  /**
    * Backing property for the name
    */
  val nameProperty = StringProperty(initName)

  /**
    * Backing property for the id
    */
  val idProperty = ObjectProperty(initId)

  /**
    * Backing property for the list of features
    */
  val featuresProperty = ObjectProperty(ObservableBuffer(initFeatures: _*))

  /**
    * The name of the motif
    */
  override def name: String = nameProperty()

  /**
    * The id of the motif
    */
  override def id: ThematicElementID = idProperty()

  /**
    * The list of rules of the node
    */
  override def features: List[String] = featuresProperty().toList

  override def toString: String = {
    val featuresString = features match {
      case Nil => "Nil"
      case _ => s"""List(
                    |    ${features map (_.toString) mkString ",\n        "})"""
    }
    s"""UiNode(
        |  id = $id,
        |  name = $name,
        |  rules = $featuresString""".stripMargin
  }

  def copy = new UIMotif(id, name, features)
}

object UIMotif {
  def apply(id: ThematicElementID,
            initName: String,
            initFeatures: List[String]) = new UIMotif(id, initName, initFeatures)
}
