package org.narrativeandplay.hypedyn.dialogs

import java.io.{DataOutputStream, DataInputStream}
import javafx.collections.ObservableList
import javafx.{event => jfxe}
import javafx.event.{ActionEvent => JfxActionEvent, EventHandler}
import javafx.scene.control.{IndexRange => JfxIndexRange}
import javafx.scene.{input => jfxsi}
import javafx.scene.input.{KeyEvent => JfxKeyEvent}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.{Event, ActionEvent}
import scalafx.geometry.{Pos, Insets, Orientation}
import scalafx.scene.control._
import scalafx.scene.input.{MouseEvent, KeyEvent}
import scalafx.scene.layout._
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control.Tab.sfxTab2jfx

import javafx.collections.ObservableList
import javafx.scene.control.IndexRange
import javafx.scene.{control => jfxsc}

import org.fxmisc.easybind.EasyBind
import org.narrativeandplay.hypedyn.story.UIMotif
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

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.{HBox, Priority, VBox}

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

  // create properties to observe changes (not sure how yet)
  //private val motif: ObjectProperty[UIMotif] = ObjectProperty(motifToEdit getOrElse UIMotif(ThematicElementID(-1),motifNameField.text(),
  //  List[String]()))
  //private[this] val monadicMotif = EasyBind monadic motif

  val featuresList = new ListView[String] {
    cellFactory = { _ =>
      new ListCell[String] {
        item onChange { (_, _, nullableFeature) =>
          Option(nullableFeature) match {
            case Some(feature) =>
              val nameField = new TextField {
                //text <==> feature

                focused onChange { (_, _, nullableIsFocused) =>
                  Option(nullableIsFocused) foreach { isFocused =>
                    selectionModel().select(feature)
                  }
                }

                HBox.setHgrow(this, Priority.Always)
              }
              val removeButton = new Button("−") {
                onAction = { _ =>
                  //motif().featuresProperty() -= feature
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
    //items <== monadicMotif flatMap[String] (_.featuresProperty) //flatMap[ObservableList[String]] (_.featuresProperty)
  }

  val featuresListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add feature") {

        onAction = { _ =>
          //val newFeature = "new feature"
          //motif().featuresProperty() += newFeature
          //featuresList.selectionModel().select(newFeature)
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

  motifToEdit foreach { m =>
    motifNameField.text = m.name
  }

  okButton.disable <== motifNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      // temporarily pass in empty lists, eventually will get from the dialog
      UIMotif(motifToEdit map (_.id) getOrElse ThematicElementID(-1),motifNameField.text(),
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
