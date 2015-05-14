package org.narrativeandplay.hypedyn.plugins.storyviewer.components

import javafx.scene.Node
import javafx.scene.control.Skin

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.{StackPane, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

class ViewerNodeSkin(node: ViewerNode) extends Skin[ViewerNode] {
  private val headingHeight: Double = 40
  private val textPadding = 10
  private val selectionOutlineWidth = 5

  // The -1 is for the rectangle to look nicer
  private val headingBarRect = new Rectangle {
    width = node.width - 1
    height = headingHeight
    fill = Color.LightGrey
    stroke = Color.Black
  }
  private val contentRect = new Rectangle {
    width = node.width
    height = node.height
    fill = Color.White
    stroke = Color.Black
  }
  private val selectRect = new Rectangle {
    width = node.width + 2 * selectionOutlineWidth
    height = node.height + 2 * selectionOutlineWidth
    fill = Color.Red
    translateX = -selectionOutlineWidth
    translateY = -selectionOutlineWidth

    visible <== node.selectedProperty
  }

  private val nodeName = new Label {
    wrapText = true
    alignment = Pos.Center

    maxWidth = node.width
    maxHeight = headingHeight

    text <== node.nameProperty
  }
  private val nodeContent = new Label {
    translateX = textPadding
    translateY = textPadding + headingHeight

    wrapText = true
    alignment = Pos.TopLeft
    //textOverrun = OverrunStyle.WordEllipsis

    delegate.setMaxSize(node.width - 2 * textPadding, node.height - 2 * textPadding - headingHeight)

    text <== node.contentProperty
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

  override def getSkinnable: ViewerNode = node

  override def getNode: Node = root
}
