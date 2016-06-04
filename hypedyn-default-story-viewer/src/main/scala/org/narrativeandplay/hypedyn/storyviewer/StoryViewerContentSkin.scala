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
        case (path, Some(highlight), Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(highlight, path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, None, Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, _, _, _, arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
      }
    }

    storyViewerContent.thematiclinks foreach { link =>
      link.draw match {
        case (path, Some(highlight), Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(highlight, path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, None, Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, _, _, _, arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
      }
    }

    storyViewerContent.themesubthemelinks foreach { link =>
      link.draw match {
        case (path, Some(highlight), Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(highlight, path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, None, Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, _, _, _, arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
      }
    }

    storyViewerContent.thememotiflinks foreach { link =>
      link.draw match {
        case (path, Some(highlight), Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(highlight, path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, None, Some(label), Some(labelBg), arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
          root.children.addAll(labelBg, label)
        case (path, _, _, _, arrowhead) =>
          root.children.addAll(path)
          root.children += arrowhead
      }
    }

    root
  }
}
