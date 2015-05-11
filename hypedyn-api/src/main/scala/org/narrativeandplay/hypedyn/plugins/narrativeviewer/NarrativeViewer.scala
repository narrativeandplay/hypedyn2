package org.narrativeandplay.hypedyn.plugins.narrativeviewer

import scalafx.scene.control.Control

import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.Nodal

trait NarrativeViewer {
  /**
   * Ensure that a NarrativeViewer is also a ScalaFX control, and a Plugin
   */
  this: Control with Plugin =>

  /**
   * Defines what to do when a node is created
   *
   * @param node The created node
   */
  def onNodeCreated(node: Nodal): Unit

  /**
   * Defines what to do when a node is updated
   *
   * @param node The node to be updated
   * @param updatedNode The same node with the updates already applied
   */
  def onNodeUpdated(node: Nodal, updatedNode: Nodal): Unit

  /**
   * Defines what to do when a node is destroyed
   *
   * @param node The node to be destroyed
   */
  def onNodeDestroyed(node: Nodal): Unit
}
