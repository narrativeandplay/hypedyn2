package org.narrativeandplay.hypedyn.story

import scalafx.Includes._
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

class UiNode(val id: NodeId,
             initName: String,
             initContent: UiNodeContent,
             initIsStartNode: Boolean,
             initRules: List[UiRule]) extends Nodal {
  val nameProperty = StringProperty(initName)
  val contentProperty = ObjectProperty(initContent)
  val isStartNodeProperty = BooleanProperty(initIsStartNode)
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

  override def toString: String = s"UiNode($id, $name, $content, isStartNode = $isStartNode, $rules)"
}

object UiNode {
  def apply(id: NodeId,
            initName: String,
            initContent: UiNodeContent,
            initIsStartNode: Boolean,
            initRules: List[UiRule]) = new UiNode(id, initName, initContent, initIsStartNode, initRules)
}
