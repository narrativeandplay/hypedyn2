package org.narrativeandplay.hypedyn.uicomponents

import scalafx.Includes._
import scalafx.geometry.Orientation
import scalafx.scene.control.SplitPane

import org.narrativeandplay.hypedyn.plugins.storyviewer.NarrativeViewersController

object CentrePane extends SplitPane {
  sealed trait CentrePaneComponent
  case object FactsPane extends CentrePaneComponent {
    def isShown = items.contains(factsAndNodesPane)
  }
  case object NodesPane extends CentrePaneComponent

  private lazy val factsAndNodesPane = new SplitPane() {
    orientation = Orientation.VERTICAL

    maxWidth = 300

    items += FactViewer
  }

  items += factsAndNodesPane
  items += NarrativeViewersController.DefaultViewer

  def hide(pane: CentrePaneComponent): Unit = {
    pane match {
      case FactsPane => items.remove(factsAndNodesPane)
      case _ =>
    }
  }
  def show(pane: CentrePaneComponent): Unit = {
    pane match {
      case FactsPane => items.prepend(factsAndNodesPane)
      case _ =>
    }
  }
}
