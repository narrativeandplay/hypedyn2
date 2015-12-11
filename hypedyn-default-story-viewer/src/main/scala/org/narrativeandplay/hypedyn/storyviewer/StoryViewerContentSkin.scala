package org.narrativeandplay.hypedyn.storyviewer

import javafx.scene.Node
import javafx.scene.control.Skin

import scalafx.Includes._
import scalafx.scene.layout.Pane

/**
 * The MVC view for the content
 *
 * @param storyViewerContent The model for the view
 */
class StoryViewerContentSkin(storyViewerContent: StoryViewerContent) extends Skin[StoryViewerContent] {
  private val root = new Pane

  override def dispose(): Unit = {}

  override def getSkinnable: StoryViewerContent = storyViewerContent

  override def getNode: Node = {
    root.children.clear()

    storyViewerContent.links foreach { link =>
      link.draw match {
        case (path, Some(highlight), Some(label), Some(labelBg), arrowheads) =>
          root.children.addAll(highlight, path)
          arrowheads foreach (root.children += _)
          root.children.addAll(labelBg, label)
        case (path, None, Some(label), Some(labelBg), arrowheads) =>
          root.children.addAll(path)
          arrowheads foreach (root.children += _)
          root.children.addAll(labelBg, label)
        case (path, _, _, _, arrowheads) =>
          root.children.addAll(path)
          arrowheads foreach (root.children += _)
      }
    }

    root
  }
}
