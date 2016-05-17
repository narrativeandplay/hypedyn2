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
  * View (as in an MVC view) for the visual representation of a motif
  *
  * @param viewerMotif
  */
class ViewerMotifSkin(viewerMotif: ViewerMotif) extends Skin[ViewerMotif] {
  import ViewerMotifSkin._

  // The -1 is for the rectangle to look nicer
  private val headingBarRect = new Rectangle {
    width <== viewerMotif.widthProperty() - 1
    height <== viewerMotif.heightProperty()
    fill = Color.Ivory
    stroke = Color.Black
  }
  private val selectRect = new Rectangle {
    width <== viewerMotif.widthProperty() + 2 * SelectionOutlineWidth
    height <== viewerMotif.heightProperty() + 2 * SelectionOutlineWidth
    fill = Color.Red
    translateX = -SelectionOutlineWidth
    translateY = -SelectionOutlineWidth

    visible <== viewerMotif.selected
  }

  private val motifName = new Label {
    wrapText = false
    alignment = Pos.Center
    textAlignment = TextAlignment.Center

    maxWidth <== viewerMotif.widthProperty()
    maxHeight <== viewerMotif.heightProperty()

    text <== viewerMotif.motifName

    visible <== viewerMotif.showName
  }

  private val headingBar = new StackPane {
    children += headingBarRect
    children += motifName
  }

  private val root = new Pane {
    children += selectRect
    children += headingBar
  }

  override def dispose(): Unit = {}

  override def getSkinnable: ViewerMotif = viewerMotif

  override def getNode: Node = root
}

object ViewerMotifSkin {
  private val TextPadding = 10
  private val SelectionOutlineWidth = 5
}
