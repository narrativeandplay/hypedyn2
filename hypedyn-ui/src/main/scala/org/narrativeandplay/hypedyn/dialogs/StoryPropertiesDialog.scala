package org.narrativeandplay.hypedyn.dialogs

import javafx.beans.value.ObservableValue
import javafx.scene.control

import scalafx.Includes._
import scalafx.geometry.Orientation
import scalafx.scene.control._
import scalafx.scene.layout.HBox
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{FileChooser, Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.easybind.EasyBind
import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.dialogs.StoryPropertiesDialog.FileSelectorWithTextField
import org.narrativeandplay.hypedyn.story.Narrative
import org.narrativeandplay.hypedyn.story.UiStory.UiStoryMetadata
import org.narrativeandplay.hypedyn.story.InterfaceToUiImplementation._
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._

/**
 * Dialog for editing story properties
 *
 * @param story The story for which to edit properties
 * @param ownerWindow The parent window for the dialog, to inherit icons
 */
class StoryPropertiesDialog(story: Narrative, ownerWindow: Window) extends Dialog[UiStoryMetadata] {
  import Narrative.ReaderStyle._

  title = "Properties"
  headerText = None

  initOwner(ownerWindow)

  // HACK: make story properties always on top
  // Possibly due to some touch screen issues
  dialogPane().scene().window().asInstanceOf[javafx.stage.Stage].alwaysOnTop = true

  val metadata: UiStoryMetadata = story.metadata

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)

  dialogPane().scene().stylesheets += getClass.getResource("/org/narrativeandplay/hypedyn/tab-pane-fix.css").toExternalForm

  val contentPane = new TabPane {
    tabs += storyPropertiesTab
    tabs += readerPropertiesTab
  }

  lazy val storyPropertiesTab = new Tab {
    text = "Story"
    closable = false

    content = new MigPane("fill") {
      add(new Label("Title: "))
      add(titleField, "growx, wrap")
      add(new Label("Author: "))
      add(authorField, "growx, wrap")
      add(new Label("Description: "))
      add(descriptionArea, "wrap")
      add(new Label("Comments: "))
      add(commentsArea)
    }
  }

  lazy val titleField = new TextField {
    text <==> metadata.titleProperty
  }
  lazy val authorField = new TextField {
    text <==> metadata.authorProperty
  }
  lazy val descriptionArea = new TextArea() {
    wrapText = true

    text <==> metadata.descriptionProperty
  }
  lazy val commentsArea = new TextArea {
    wrapText = true
    text <==> metadata.commentsProperty
  }

  lazy val readerPropertiesTab = new Tab {
    text = "Reader"
    closable = false

    content = new MigPane("fill") {
      add(new Label("Style"), "wrap")
      add(new HBox {
        children += standardRadio
        children += fancyRadio
        children += customRadio
      }, "wrap")
      add(customCssFileSelector, "wrap")
      add(new control.Separator(Orientation.Horizontal), "growx, wrap")
      add(new Label("Control"), "wrap")
      add(backDisabledCheckbox, "wrap")
      add(restartDisabledCheckbox)
    }
  }

  lazy val readerStyleRadioGroup = new ToggleGroup
  lazy val standardRadio = new RadioButton("Standard") {
    userData = Narrative.ReaderStyle.Standard
    toggleGroup = readerStyleRadioGroup
  }
  lazy val fancyRadio = new RadioButton("Fancy") {
    userData = Narrative.ReaderStyle.Fancy
    toggleGroup = readerStyleRadioGroup
  }
  lazy val customRadio = new RadioButton("Custom") {
    userData = Narrative.ReaderStyle.Custom("")
    toggleGroup = readerStyleRadioGroup
  }

  lazy val customCssFileSelector = new FileSelectorWithTextField(dialogPane().getScene.getWindow) {
    chooseButton.disable <== !customRadio.selected
    filePathField.disable <== !customRadio.selected
  }

  lazy val backDisabledCheckbox = new CheckBox("Disable back button") {
    allowIndeterminate = false
    selected <==> metadata.backDisabledProperty
  }
  lazy val restartDisabledCheckbox = new CheckBox("Disable restart button") {
    allowIndeterminate = false
    selected <==> metadata.restartDisabledProperty
  }

  okButton.disable <== EasyBind combine (customRadio.selected.delegate.asInstanceOf[ObservableValue[Boolean]],
                                         customCssFileSelector.filePathField.text,
                                         { (selected: Boolean, path: String) =>
    Boolean box (selected && path.trim.isEmpty)
  })

  metadata.readerStyle match {
    case Standard => standardRadio.selected = true
    case Fancy => fancyRadio.selected = true
    case Custom(path) =>
      customRadio.selected = true
      customCssFileSelector.filePathField.text = path
  }

  dialogPane().content = contentPane

  resultConverter = {
    case ButtonType.OK =>
      import Narrative.ReaderStyle._
      val readerStyle = readerStyleRadioGroup.selectedToggle().userData.asInstanceOf[Narrative.ReaderStyle] match {
        case Standard => Standard
        case Fancy => Fancy
        case Custom(_) => Custom(customCssFileSelector.selectedFileFilename)
      }
      metadata.readerStyleProperty() = readerStyle
      metadata
    case _ => null
  }

  /**
   * Shows a blocking story properties dialog
   *
   * @return An option containing the result of the dialog, or None if the dialog was not closed using the OK button
   */
  def showAndWait(): Option[UiStoryMetadata] = {
    initModality(Modality.ApplicationModal)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}

object StoryPropertiesDialog {

  /**
   * A control that integrates a text field with a file selector
   *
   * @param ownerWindow The parent window of the control, for the file dialog to inherit icons
   */
  class FileSelectorWithTextField(ownerWindow: Window) extends HBox {
    private val fileDialog = new FileDialog(ownerWindow) {
      extensionFilters.clear()
      extensionFilters += new ExtensionFilter("CSS File", "*.css")
    }

    val filePathField = new TextField {
      editable = false
      text = ""
    }
    val chooseButton = new Button("Choose") {
      onAction = { _ =>
        fileDialog.showOpenFileDialog() foreach { file => filePathField.text = file.getAbsolutePath }
      }
    }

    children += filePathField
    children += chooseButton

    def selectedFileFilename = filePathField.text()
  }
}
