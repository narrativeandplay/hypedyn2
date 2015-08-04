package org.narrativeandplay.hypedyn.dialogs

import scalafx.Includes._
import scalafx.geometry.Pos
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx

import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.story.Narrative

class StoryPropertiesDialog(story: Narrative, ownerWindow: Window) extends Dialog[(String, String, String)] {
  title = "Properties"
  headerText = None

  initOwner(ownerWindow)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)

  val contentPane = new TabPane {
    tabs += storyPropertiesTab
  }

  lazy val storyPropertiesTab = new Tab {
    text = "Story"
    closable = false

    content = new MigPane("fill") {
      add(new Label("Title: "))
      add(titleField, "wrap")
      add(new Label("Author: "))
      add(authorField, "wrap")
      add(new Label("Description: "))
      add(descriptionArea)
    }
  }

  lazy val titleField = new TextField {
    text = story.title
  }
  lazy val authorField = new TextField {
    text = story.author
  }
  lazy val descriptionArea = new TextArea(story.description) {
    wrapText = true
  }

  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK => (titleField.text(), authorField.text(), descriptionArea.text())
    case _ => null
  }

  def showAndWait(): Option[(String, String, String)] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
