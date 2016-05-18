package org.narrativeandplay.hypedyn.dialogs

import javafx.scene.{control => jfxsc}

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

  // create properties to observe changes (not sure how yet)
  //private val theme: ObjectProperty[UITheme] = ObjectProperty(themeToEdit getOrElse UITheme(ThematicElementID(-1),themeNameField.text(),
  //  List[ThematicElementID](),List[ThematicElementID]()))
  //private[this] val monadicTheme = EasyBind monadic theme

  val subthemesList = new ListView[ThematicElementID] {
    cellFactory = { _ =>
      new ListCell[ThematicElementID] {
        item onChange { (_, _, nullableSubtheme) =>
          Option(nullableSubtheme) match {
            case Some(subtheme) =>
              val nameField = new TextField {
                //text <==> subtheme.value.toString()

                focused onChange { (_, _, nullableIsFocused) =>
                  Option(nullableIsFocused) foreach { isFocused =>
                    selectionModel().select(subtheme)
                  }
                }

                HBox.setHgrow(this, Priority.Always)
              }
              val removeButton = new Button("−") {
                onAction = { _ =>
                  //motif().featuresProperty() -= subtheme
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

    themeNameField.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    // add the items?
    //items <== monadicTheme flatMap[String] (_.featuresProperty) //flatMap[ObservableList[String]] (_.featuresProperty)
  }

  val subthemesListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add subtheme") {

        onAction = { _ =>
        }
      }
    }
    children += subthemesList

    VBox.setVgrow(subthemesList, Priority.Always)
  }

  val motifsList = new ListView[ThematicElementID] {
    cellFactory = { _ =>
      new ListCell[ThematicElementID] {
        item onChange { (_, _, nullableMotif) =>
          Option(nullableMotif) match {
            case Some(motif) =>
              val nameField = new TextField {
                //text <==> motif

                focused onChange { (_, _, nullableIsFocused) =>
                  Option(nullableIsFocused) foreach { isFocused =>
                    selectionModel().select(motif)
                  }
                }

                HBox.setHgrow(this, Priority.Always)
              }
              val removeButton = new Button("−") {
                onAction = { _ =>
                  //motif().featuresProperty() -= motif
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

    themeNameField.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    // add the items?
    //items <== monadicTheme flatMap[String] (_.featuresProperty) //flatMap[ObservableList[String]] (_.featuresProperty)
  }

  val motifsListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add motif") {

        onAction = { _ =>
        }
      }
    }
    children += motifsList

    VBox.setVgrow(motifsList, Priority.Always)
  }

  val subthemesAndMotifsPane = new TabPane {
    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

    tabs += new Tab {
      text = "Subthemes"
      content = subthemesListVBox
    }
    tabs += new Tab {
      text = "Motifs"
      content = motifsListVBox
    }
  }

  val contentPane= new BorderPane() {
    top = new VBox() {
      children += new HBox(10) {
        padding = Insets(5, 0, 5, 0)
        alignment = Pos.CenterLeft

        children += new Label("Name: ")
        children += themeNameField

        HBox.setHgrow(themeNameField, Priority.Always)
      }
    }

    center = subthemesAndMotifsPane
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
