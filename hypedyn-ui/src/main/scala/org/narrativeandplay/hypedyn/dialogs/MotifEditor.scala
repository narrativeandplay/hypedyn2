package org.narrativeandplay.hypedyn.dialogs

import javafx.collections.ObservableList
import javafx.event.{ActionEvent => JfxActionEvent}
import javafx.scene.control.{IndexRange => JfxIndexRange}
import javafx.scene.input.{KeyEvent => JfxKeyEvent}
import javafx.scene.{control => jfxsc, input => jfxsi}
import javafx.{event => jfxe}

import org.fxmisc.easybind.EasyBind
import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.story.UIMotif
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, Priority, VBox, _}
import scalafx.stage.{Modality, Window}

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

  private val motif: ObjectProperty[UIMotif] = ObjectProperty(motifToEdit match {
    case Some(motifToEdit) =>
      UIMotif(motifToEdit.id, motifToEdit.name, motifToEdit.features)
    case None =>
      UIMotif(ThematicElementID(-1),"", List[String]())
  })

  val featuresList = new ListView[ObjectProperty[String]] {
    cellFactory = { _ =>
      new ListCell[ObjectProperty[String]] {
        item onChange { (_, _, nullableFeature) =>
          Option(nullableFeature) match {
            case Some(feature) =>
              val nameField = new TextField {
                text <==> feature

                focused onChange { (_, _, nullableIsFocused) =>
                  Option(nullableIsFocused) foreach { isFocused =>
                    selectionModel().select(feature)
                  }
                }

                HBox.setHgrow(this, Priority.Always)
              }
              val removeButton = new Button("−") {
                onAction = { _ =>
                  items() -= feature
                  motif().featuresProperty() -= feature
                }
              }

              graphic = new HBox(10, removeButton, nameField)
            case None => graphic = null
          }
        }
      }
    }

    selectionModel().selectedItemProperty onChange { (_, _, `new`) =>
      Option(`new`) match {
        case Some(feature) =>
        case None =>
      }
    }

    motifNameField.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    // add the items?
    motif().featuresProperty().map(items() += ObjectProperty(_).apply()) // adds but doesn't observe
    //items() = motif().featuresProperty().map[ObservableList[ObjectProperty[String]]](_:String => _)
  }

  val featuresListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add feature") {

        onAction = { _ =>
          Logger.info("**** new feature ****")
          val newFeature = ObjectProperty("new feature")
          motif().featuresProperty() += newFeature
          featuresList.items()+=newFeature
          featuresList.selectionModel().select(newFeature)
        }
      }
    }
    children += featuresList

    VBox.setVgrow(featuresList, Priority.Always)
  }


  val contentPane= new BorderPane() {
    top = new VBox() {
      children += new HBox(10) {
        padding = Insets(5, 0, 5, 0)
        alignment = Pos.CenterLeft

        children += new Label("Name: ")
        children += motifNameField

        HBox.setHgrow(motifNameField, Priority.Always)
      }
    }

    center = featuresListVBox
  }
  dialogPane().content = contentPane

  motifNameField.text <==> motif().nameProperty

  okButton.disable <== motifNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      motif()
      // temporarily pass in empty lists, eventually will get from the dialog
      //UIMotif(motifToEdit map (_.id) getOrElse ThematicElementID(-1),motifNameField.text(),
      //  List[String]())
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
