package org.narrativeandplay.hypedyn.uicomponents

import scalafx.Includes._
import scalafx.geometry.Orientation
import scalafx.scene.Group
import scalafx.scene.control.{Label, Button, ToolBar}

object Sidebar extends ToolBar {
  orientation = Orientation.VERTICAL

  class SidebarButton(labelText: String) extends Button {
    private val buttonLabel = new Label(labelText) {
      rotate = -90
    }

    graphic = new Group(buttonLabel)
  }

  private lazy val factsButton = new SidebarButton("Facts") {
    onAction = { _ =>
      if (CentrePane.FactsPane.isShown) CentrePane.hide(CentrePane.FactsPane) else CentrePane.show(CentrePane.FactsPane)
    }
  }

  items += factsButton

}
