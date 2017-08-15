package org.narrativeandplay.hypedyn.ui.story

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

import org.narrativeandplay.hypedyn.api.story.{Nodal, NodeId}

/**
 * UI implementation for Nodal
 * @param id The ID of the node
 * @param initName The initial name of the node
 * @param initContent The initial content of the node
 * @param initIsStartNode The initial value of whether this node was the start node of the story
 * @param initRules The initial list of rules
 */
class UiNode(val id: NodeId,
             initName: String,
             initContent: UiNodeContent,
             initIsStartNode: Boolean,
             initRules: List[UiRule]) extends Nodal {
  /**
   * Backing property for the name
   */
  val nameProperty = StringProperty(initName)

  /**
   * Backing property for the content
   */
  val contentProperty = ObjectProperty(initContent)

  /**
   * Backing property for the start node status
   */
  val isStartNodeProperty = BooleanProperty(initIsStartNode)

  /**
   * Backing property for the list of rules
   */
  val rulesProperty = ObjectProperty(ObservableBuffer(initRules: _*))
  
  /**
   * The name of the node
   */
  override def name: String = nameProperty()

  /**
   * The list of rules of the node
   */
  override def rules: List[UiRule] = rulesProperty().toList

  /**
   * The content of the node
   */
  override def content: UiNodeContent = contentProperty()

  /**
   * Determines if this node represents the start of the story
   */
  override def isStartNode: Boolean = isStartNodeProperty()

  override def toString: String = {
    val rulesString = rules match {
      case Nil => "Nil"
      case _ => s"""List(
                   |    ${rules map (_.toString) mkString ",\n        "})"""
    }
    s"""UiNode(
       |  id = $id,
       |  name = $name,
       |  content = $content,
       |  isStartNode = $isStartNode,
       |  rules = $rulesString""".stripMargin
  }

  def copy = new UiNode(id, name, content, isStartNode, rules)
}

object UiNode {
  def apply(id: NodeId,
            initName: String,
            initContent: UiNodeContent,
            initIsStartNode: Boolean,
            initRules: List[UiRule]) = new UiNode(id, initName, initContent, initIsStartNode, initRules)
}
