package org.narrativeandplay.hypedyn.plugins.narrativeviewer

import scalafx.scene.control.Control
import org.narrativeandplay.hypedyn.events.EventBus
import org.narrativeandplay.hypedyn.plugins.Plugin
import org.narrativeandplay.hypedyn.story.Nodal
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThemeLike}

/**
 * An interface for a plugin that allows for story visualisation
 */
trait NarrativeViewer {
  /**
   * Ensure that a NarrativeViewer is also a ScalaFX control, and a Plugin
   */
  self: Control with Plugin =>

  /**
   * The plugin is automatically hooked into the appropriate event streams
   */
  EventBus.NodeCreatedEvents foreach { n => onNodeCreated(n.node) }
  EventBus.NodeUpdatedEvents foreach { n => onNodeUpdated(n.node, n.updatedNode) }
  EventBus.NodeDestroyedEvents foreach { n => onNodeDestroyed(n.node) }
  EventBus.ThemeCreatedEvents foreach { t => onThemeCreated(t.theme) }
  EventBus.ThemeUpdatedEvents foreach { t => onThemeUpdated(t.theme, t.updatedTheme) }
  EventBus.ThemeDestroyedEvents foreach { t => onThemeDestroyed(t.theme) }
  EventBus.MotifCreatedEvents foreach { m => onMotifCreated(m.motif) }
  EventBus.MotifUpdatedEvents foreach { m => onMotifUpdated(m.motif, m.updatedMotif) }
  EventBus.MotifDestroyedEvents foreach { m => onMotifDestroyed(m.motif) }

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

  /**
    * Defines what to do when a theme is created
    *
    * @param theme The created theme
    */
  def onThemeCreated(theme: ThemeLike): Unit

  /**
    * Defines what to do when a theme is updated
    *
    * @param theme The theme to be updated
    * @param updatedTheme The same theme with the updates already applied
    */
  def onThemeUpdated(theme: ThemeLike, updatedTheme: ThemeLike): Unit

  /**
    * Defines what to do when a theme is destroyed
    *
    * @param theme The theme to be destroyed
    */
  def onThemeDestroyed(theme: ThemeLike): Unit

  /**
    * Defines what to do when a motif is created
    *
    * @param motif The created motif
    */
  def onMotifCreated(motif: MotifLike): Unit

  /**
    * Defines what to do when a motif is updated
    *
    * @param motif The motif to be updated
    * @param updatedMotif The same motif with the updates already applied
    */
  def onMotifUpdated(motif: MotifLike, updatedMotif: MotifLike): Unit

  /**
    * Defines what to do when a motif is destroyed
    *
    * @param motif The motif to be destroyed
    */
  def onMotifDestroyed(motif: MotifLike): Unit}
