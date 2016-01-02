package org.narrativeandplay.hypedyn.uicomponents

import scalafx.Includes._
import scalafx.geometry.Orientation
import scalafx.scene.Group
import scalafx.scene.control.{Label, Button, ToolBar}

/**
 * The side bar for the application
 */
object Sidebar extends ToolBar {
  orientation = Orientation.VERTICAL

  class SidebarButton(labelText: String) extends Button {
    private val buttonLabel = new Label(labelText) {
      rotate = -90
    }

    def buttonText = buttonLabel.text
    def buttonText_=(inText: String): Unit = {
      buttonLabel.text = inText
    }

    graphic = new Group(buttonLabel)
  }

  private lazy val factsButton = new SidebarButton("Hide facts") {
    onAction = { _ =>
      if (CentrePane.FactsPane.isShown) {
        CentrePane.hide(CentrePane.FactsPane)
        buttonText = "Show facts"
      }
      else {
        CentrePane.show(CentrePane.FactsPane)
        buttonText = "Hide facts"
      }
    }
  }

  items += factsButton

}
