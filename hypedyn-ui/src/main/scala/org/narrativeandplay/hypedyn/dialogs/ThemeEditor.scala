package org.narrativeandplay.hypedyn.dialogs

import javafx.scene.{control => jfxsc}

import scala.util.Try
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.stage.{Modality, Window}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx
import org.tbee.javafx.scene.layout.MigPane
import org.narrativeandplay.hypedyn.story.themes.{ThematicElementID, ThemeLike}
import org.narrativeandplay.hypedyn.story.themes.internal.Theme

/**
  * Dialog for editing themes
  *
  * @param dialogTitle The title of the dialog
  * @param themeToEdit An option containing the theme to edit, or None if a new theme is to be created
  * @param ownerWindow The parent window of the dialog, for inheriting icons
  */
class ThemeEditor private (dialogTitle: String,
                          themeToEdit: Option[ThemeLike],
                          ownerWindow: Window) extends Dialog[ThemeLike] {
  /**
    * Creates a new dialog for creating a theme
    *
    * @param dialogTitle The title of the dialog
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new fact dialog
    */
  def this(dialogTitle: String, ownerWindow: Window) =
    this(dialogTitle, None, ownerWindow)

  /**
    * Creates a new dialog for editing a theme
    *
    * @param dialogTitle The title of the dialog
    * @param themeToEdit The theme to edit
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new theme dialog
    */
  def this(dialogTitle: String, themeToEdit: ThemeLike, ownerWindow: Window) =
    this(dialogTitle, Some(themeToEdit), ownerWindow)

  title = dialogTitle
  headerText = None

  initOwner(ownerWindow)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)

  private val themeNameField = new TextField()

  private val contentPane: MigPane = new MigPane("fill") {
    add(new Label("Name: "))
    add(themeNameField, "wrap")
  }
  dialogPane().content = contentPane

  themeToEdit foreach { t =>
    themeNameField.text = t.name
  }

  okButton.disable <== themeNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      // temporarily pass in empty lists, eventually will get from the dialog
      Theme(themeToEdit map (_.id) getOrElse ThematicElementID(-1),themeNameField.text(),
        List[ThematicElementID](), List[ThematicElementID]())
    case _ => null
  }

  /**
    * Shows a blocking theme editor dialog
    *
    * @return An option containing the result of the dialog, or None if the dialog was not closed using
    *         the OK button
    */
  def showAndWait(): Option[ThemeLike] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
