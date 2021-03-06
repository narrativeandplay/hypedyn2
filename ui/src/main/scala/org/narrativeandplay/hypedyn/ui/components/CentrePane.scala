package org.narrativeandplay.hypedyn.ui.components

import scalafx.Includes._
import scalafx.geometry.Orientation
import scalafx.scene.control.SplitPane

import org.narrativeandplay.hypedyn.ui.plugins.storyviewer.NarrativeViewersController

/**
 * Container for the components in the centre pane
 */
object CentrePane extends SplitPane {
  sealed trait CentrePaneComponent
  case object FactsPane extends CentrePaneComponent {
    def isShown = items.contains(factsAndNodesPane)
  }
  case object NodesPane extends CentrePaneComponent

  private lazy val factsAndNodesPane = new SplitPane() {
    orientation = Orientation.Vertical

    maxWidth = 300

    items += FactViewer
  }

  items += factsAndNodesPane
  items += NarrativeViewersController.DefaultViewer

  /**
   * Hide a component
   *
   * @param pane The pane to hide
   */
  def hide(pane: CentrePaneComponent): Unit = {
    pane match {
      case FactsPane => items.remove(factsAndNodesPane)
      case _ =>
    }
  }

  /**
   * Show a component
   *
   * @param pane The pane to show
   */
  def show(pane: CentrePaneComponent): Unit = {
    pane match {
      case FactsPane => items.prepend(factsAndNodesPane)
      case _ =>
    }
  }
}
