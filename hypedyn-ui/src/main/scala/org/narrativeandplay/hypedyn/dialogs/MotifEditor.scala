package org.narrativeandplay.hypedyn.dialogs

import javafx.scene.{control => jfxsc}

import org.narrativeandplay.hypedyn.story.themes.internal.Motif
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID}

import scala.util.Try
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.stage.{Modality, Window}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx
import org.tbee.javafx.scene.layout.MigPane

/**
  * Dialog for editing themes
  *
  * @param dialogTitle The title of the dialog
  * @param motifToEdit An option containing the motif to edit, or None if a new motif is to be created
  * @param ownerWindow The parent window of the dialog, for inheriting icons
  */
class MotifEditor private (dialogTitle: String,
                           motifToEdit: Option[MotifLike],
                           ownerWindow: Window) extends Dialog[MotifLike] {
  /**
    * Creates a new dialog for creating a motif
    *
    * @param dialogTitle The title of the dialog
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new fact dialog
    */
  def this(dialogTitle: String, ownerWindow: Window) =
    this(dialogTitle, None, ownerWindow)

  /**
    * Creates a new dialog for editing a motif
    *
    * @param dialogTitle The title of the dialog
    * @param motifToEdit The motif to edit
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new motif dialog
    */
  def this(dialogTitle: String, motifToEdit: MotifLike, ownerWindow: Window) =
    this(dialogTitle, Some(motifToEdit), ownerWindow)

  title = dialogTitle
  headerText = None

  initOwner(ownerWindow)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)

  private val motifNameField = new TextField()

  private val contentPane: MigPane = new MigPane("fill") {
    add(new Label("Name: "))
    add(motifNameField, "wrap")
  }
  dialogPane().content = contentPane

  motifToEdit foreach { m =>
    motifNameField.text = m.name
  }

  okButton.disable <== motifNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      // temporarily pass in empty lists, eventually will get from the dialog
      Motif(motifToEdit map (_.id) getOrElse ThematicElementID(-1),motifNameField.text(),
        List[String]())
    case _ => null
  }

  /**
    * Shows a blocking motif editor dialog
    *
    * @return An option containing the result of the dialog, or None if the dialog was not closed using
    *         the OK button
    */
  def showAndWait(): Option[MotifLike] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
