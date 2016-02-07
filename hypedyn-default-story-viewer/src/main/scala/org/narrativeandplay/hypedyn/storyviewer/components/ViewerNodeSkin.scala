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
 * View (as in an MVC view) for the visual representation of a node
 *
 * @param viewerNode
 */
class ViewerNodeSkin(viewerNode: ViewerNode) extends Skin[ViewerNode] {
  import ViewerNodeSkin._

  // The -1 is for the rectangle to look nicer
  private val headingBarRect = new Rectangle {
    width <== viewerNode.widthProperty() - 1
    height = HeadingHeight
    fill <== when (viewerNode.isAnywhere) choose Color.Silver otherwise Color.LightGrey
    stroke = Color.Black
  }
  private val contentRect = new Rectangle {
    width <== viewerNode.widthProperty()
    height <== viewerNode.heightProperty()
    fill = Color.White
    stroke = Color.Black
  }
  private val selectRect = new Rectangle {
    width <== viewerNode.widthProperty() + 2 * SelectionOutlineWidth
    height <== viewerNode.heightProperty() + 2 * SelectionOutlineWidth
    fill = Color.Red
    translateX = -SelectionOutlineWidth
    translateY = -SelectionOutlineWidth

    visible <== viewerNode.selected
  }

  private val nodeName = new Label {
    wrapText = true
    alignment = Pos.Center
    textAlignment = TextAlignment.Center

    maxWidth <== viewerNode.widthProperty()
    maxHeight = ViewerNodeSkin.HeadingHeight

    text <== viewerNode.nodeName
  }
  private val nodeContent = new Label {
    translateX = TextPadding
    translateY = TextPadding + HeadingHeight

    wrapText = true
    alignment = Pos.TopLeft
    //textOverrun = OverrunStyle.WordEllipsis

    maxWidth <== viewerNode.widthProperty() - 2 * TextPadding
    maxHeight <== viewerNode.heightProperty() - 2 * TextPadding - HeadingHeight

    text <== viewerNode.contentText
  }

  private val contentBox = new Pane {
    children += contentRect
    children += nodeContent
  }

  private val headingBar = new StackPane {
    children += headingBarRect
    children += nodeName
  }

  private val root = new Pane {
    children += selectRect
    children += contentBox
    children += headingBar
  }

  override def dispose(): Unit = {}

  override def getSkinnable: ViewerNode = viewerNode

  override def getNode: Node = root
}

object ViewerNodeSkin {
  private val HeadingHeight = 40d
  private val TextPadding = 10
  private val SelectionOutlineWidth = 5
}