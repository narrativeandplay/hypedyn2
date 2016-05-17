package org.narrativeandplay.hypedyn.storyviewer.components

import javafx.scene.Node
import javafx.scene.control.Skin

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.{StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.TextAlignment

/**
  * View (as in an MVC view) for the visual representation of a theme
  *
  * @param viewerTheme
  */
class ViewerThemeSkin(viewerTheme: ViewerTheme) extends Skin[ViewerTheme] {
  import ViewerThemeSkin._

  // The -1 is for the rectangle to look nicer
  private val headingBarRect = new Rectangle {
    width <== viewerTheme.widthProperty() - 1
    height = HeadingHeight
    fill = Color.LightGrey
    stroke = Color.Black
  }
  private val contentRect = new Rectangle {
    width <== viewerTheme.widthProperty()
    height <== viewerTheme.heightProperty()
    fill = Color.White
    stroke = Color.Black

    visible <== viewerTheme.showContent
  }
  private val selectRect = new Rectangle {
    width <== viewerTheme.widthProperty() + 2 * SelectionOutlineWidth
    height <== HeadingHeight + 2 * SelectionOutlineWidth
    fill = Color.Red
    translateX = -SelectionOutlineWidth
    translateY = -SelectionOutlineWidth

    visible <== viewerTheme.selected
  }

  private val themeName = new Label {
    wrapText = false
    alignment = Pos.Center
    textAlignment = TextAlignment.Center

    maxWidth <== viewerTheme.widthProperty()
    maxHeight = ViewerThemeSkin.HeadingHeight

    text <== viewerTheme.themeName

    visible <== viewerTheme.showName
  }

  private val headingBar = new StackPane {
    children += headingBarRect
    children += themeName
  }

  private val root = new Pane {
    children += selectRect
    children += headingBar
  }

  override def dispose(): Unit = {}

  override def getSkinnable: ViewerTheme = viewerTheme

  override def getNode: Node = root
}

object ViewerThemeSkin {
  private val HeadingHeight = 40d
  private val TextPadding = 10
  private val SelectionOutlineWidth = 5
}
