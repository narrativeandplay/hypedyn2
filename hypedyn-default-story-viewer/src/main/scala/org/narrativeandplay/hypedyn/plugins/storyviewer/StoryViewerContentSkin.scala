package org.narrativeandplay.hypedyn.plugins.storyviewer

import javafx.scene.Node
import javafx.scene.control.Skin

import scalafx.scene.layout.Pane

class StoryViewerContentSkin(storyViewerContent: StoryViewerContent) extends Skin[StoryViewerContent] {
  val root = new Pane

  override def getSkinnable: StoryViewerContent = storyViewerContent

  override def dispose(): Unit = {}

  override def getNode: Node = {
    root.children.clear()

    storyViewerContent.links foreach { link =>
      link.graphicElements match {
        case (path, Some(highlight), Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(highlight, path, labelBg, label, arrowhead)
        case (path, None, Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(path, labelBg, label, arrowhead)
        case (path, _, _, _, arrowhead) =>
          root.children.addAll(path, arrowhead)
      }
    }

    root
  }
}
