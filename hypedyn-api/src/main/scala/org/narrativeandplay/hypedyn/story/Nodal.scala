package org.narrativeandplay.hypedyn.story

trait Nodal extends Serializable {
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
  def content: NodeContent

  /**
   * Determines if this node represents the start of the story
   */
  def isStartNode: Boolean
}

case class NodeId(value: Long) extends AnyVal with Ordered[NodeId] {
  override def compare(that: NodeId): Int = value compare that.value

  def increment = new NodeId(value + 1)
  def inc = increment

  def isValid = value >= 0
}
