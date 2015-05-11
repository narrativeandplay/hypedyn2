package org.narrativeandplay.hypedyn.story

trait Nodal extends Serializable {
  /**
   * Gets the ID of the node.
   *
   * The intended use to maintain consistency of node IDs across all plugins without the need to maintain a mapping
   * of application node IDs to plugin node IDs
   */
  def id: Long

  /**
   * The name of the node
   */
  def name: String

  /**
   * The content of the node
   */
  def content: String

  /**
   * Determines if this node represents the start of the story
   */
  def isStartNode: Boolean
}
