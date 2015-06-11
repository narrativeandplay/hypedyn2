package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.RuleLike

trait Nodal extends NarrativeElement[Nodal] with Serializable {
  /**
   * Gets the ID of the node.
   *
   * The intended use to maintain consistency of node IDs across all plugins without the need to maintain a mapping
   * of application node IDs to plugin node IDs
   */
  def id: NodeId

  /**
   * The name of the node
   */
  def name: String

  /**
   * The content of the node
   */
  def content: NodalContent

  /**
   * Determines if this node represents the start of the story
   */
  def isStartNode: Boolean

  /**
   * The list of rules of the node
   */
  def rules: List[RuleLike]
}

case class NodeId(value: BigInt) extends AnyVal with Ordered[NodeId] {
  override def compare(that: NodeId): Int = value compare that.value

  def increment = new NodeId(value + 1)
  def inc = increment

  def isValid = value >= 0
}
