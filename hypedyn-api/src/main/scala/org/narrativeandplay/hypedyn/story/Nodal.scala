package org.narrativeandplay.hypedyn.story

import org.narrativeandplay.hypedyn.story.rules.RuleLike

/**
 * An interface for a node of the story
 */
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

/**
 * A value type for the ID of a node
 *
 * @param value The integer value of the ID
 */
case class NodeId(value: BigInt) extends AnyVal with Ordered[NodeId] {
  override def compare(that: NodeId): Int = value compare that.value

  /**
   * Returns a NodeId which has it's value incremented by one from the original
   */
  def increment = new NodeId(value + 1)

  /**
   * An alias for `increment`
   */
  def inc = increment

  /**
   * Returns a FactId which has it's value decremented by one from the original
   */
  def decrement = new NodeId(value - 1)

  /**
   * An alias for `decrement`
   */
  def dec = decrement

  /**
   * Returns true if the FactId is valid, false otherwise
   *
   * A valid node id is one whose value is greater than or equal to 0
   */
  def isValid = value >= 0
}
