package org.narrativeandplay.hypedyn.plugins.storyviewer

trait ViewerNode {
  /**
   * Returns the ID of the related node in the story structure (i.e. this is a foreign key)
   */
  def nodeId: Long
}
