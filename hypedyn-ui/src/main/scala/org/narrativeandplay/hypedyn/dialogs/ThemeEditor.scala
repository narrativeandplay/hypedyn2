package org.narrativeandplay.hypedyn.dialogs

import javafx.scene.{control => jfxsc}
import java.io.{DataInputStream, DataOutputStream}
import javafx.collections.ObservableList
import javafx.{event => jfxe}
import javafx.event.{EventHandler, ActionEvent => JfxActionEvent}
import javafx.scene.{input => jfxsi}
import javafx.scene.input.{KeyEvent => JfxKeyEvent}
import javafx.scene.control.{IndexRange => JfxIndexRange, ListCell => JfxListCell, SpinnerValueFactory => JfxSpinnerValueFactory}

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.{ObservableBuffer, ObservableMap}
import scalafx.scene.control._
import scalafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.layout._
import scalafx.stage.{Modality, Window}
import scalafx.scene.Parent.sfxParent2jfx
import scalafx.scene.control.Tab.sfxTab2jfx
import javafx.collections.ObservableList
import javafx.scene.{control => jfxsc}

import org.fxmisc.easybind.EasyBind
import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.story.rules.RuleLike.ParamName
import org.narrativeandplay.hypedyn.story.{Narrative, UIMotif, UITheme, UiStory}

import scala.util.Try
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.stage.{Modality, Window}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx
import org.tbee.javafx.scene.layout.MigPane
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID, ThemeLike}
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
  * @param story The parent story of the theme
  * @param ownerWindow The parent window of the dialog, for inheriting icons
  */
class ThemeEditor private (dialogTitle: String,
                           themeToEdit: Option[ThemeLike],
                           story: Narrative,
                           ownerWindow: Window) extends Dialog[ThemeLike] {
  /**
    * Creates a new dialog for creating a theme
    *
    * @param dialogTitle The title of the dialog
    * @param story The parent story of the theme
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new fact dialog
    */
  def this(dialogTitle: String, story: Narrative, ownerWindow: Window) =
    this(dialogTitle, None, story, ownerWindow)

  /**
    * Creates a new dialog for editing a theme
    *
    * @param dialogTitle The title of the dialog
    * @param themeToEdit The theme to edit
    * @param story The parent story of the theme
    * @param ownerWindow The parent window of the dialog, for inheriting icons
    * @return A new theme dialog
    */
  def this(dialogTitle: String, themeToEdit: ThemeLike, story: Narrative, ownerWindow: Window) =
    this(dialogTitle, Some(themeToEdit), story, ownerWindow)

  title = dialogTitle
  headerText = None
  resizable = true
  dialogPane().setPrefSize(400, 300)

  initOwner(ownerWindow)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)

  private val themeNameField = new TextField()

  private val theme: ObjectProperty[UITheme] = ObjectProperty(themeToEdit match {
    case Some(themeToEdit) =>
      UITheme(themeToEdit.id, themeToEdit.name, themeToEdit.subthemes, themeToEdit.motifs)
    case None =>
      UITheme(ThematicElementID(-1),"", List[ThematicElementID](),List[ThematicElementID]())
  })

  val subthemesList = new ListView[ObjectProperty[ThematicElementID]] {
    cellFactory = { _ =>
      new ListCell[ObjectProperty[ThematicElementID]] {
        item onChange { (_, _, nullableSubtheme) =>
          Option(nullableSubtheme) match {
            case Some(subtheme) =>
              val comboBox = new ThemeComboBox(subtheme, story)

              val removeButton = new Button("−") {
                onAction = { _ =>
                  items() -= subtheme
                  theme().subthemesProperty() -= subtheme
                }
              }

              graphic = new HBox(10, removeButton, comboBox)
            case None => graphic = null
          }
        }
      }
    }

    selectionModel().selectedItemProperty onChange { (_, _, `new`) =>
      Option(`new`) match {
        case Some(subtheme) =>
        case None =>
      }
    }

    themeNameField.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    // add the items?
    theme().subthemesProperty().map(items() += _) // adds but doesn't observe
  }

  val subthemesListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add subtheme") {

        onAction = { _ =>
          Logger.info("**** new subtheme ****")
          val newSubtheme = ObjectProperty(new ThematicElementID(-1))
          theme().subthemesProperty() += newSubtheme
          subthemesList.items()+=newSubtheme
          subthemesList.selectionModel().select(newSubtheme)
        }
      }
    }
    children += subthemesList

    VBox.setVgrow(subthemesList, Priority.Always)
  }

  val motifsList = new ListView[ObjectProperty[ThematicElementID]] {
    cellFactory = { _ =>
      new ListCell[ObjectProperty[ThematicElementID]] {
        item onChange { (_, _, nullableMotif) =>
          Option(nullableMotif) match {
            case Some(motif) =>
              val comboBox = new MotifComboBox(motif, story)

              val removeButton = new Button("−") {
                onAction = { _ =>
                  items() -= motif
                  theme().motifsProperty() -= motif
                }
              }

              graphic = new HBox(10, removeButton, comboBox)
            case None => graphic = null
          }
        }
      }
    }

    selectionModel().selectedItemProperty onChange { (_, _, `new`) =>
      Option(`new`) match {
        case Some(motif) =>
        case None =>
      }
    }

    themeNameField.focused onChange { (_, _, focus) => if (focus) selectionModel().clearSelection() }

    // add the items?
    theme().motifsProperty().map(items() += _) // adds but doesn't observe
  }

  val motifsListVBox = new VBox {
    children += new HBox {
      padding = Insets(5)
      alignment = Pos.CenterLeft
      children += new Button("Add motif") {

        onAction = { _ =>
          Logger.info("**** new motif ****")
          val newMotif = ObjectProperty(new ThematicElementID(-1))
          theme().motifsProperty() += newMotif
          motifsList.items()+=newMotif
          motifsList.selectionModel().select(newMotif)
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

  themeNameField.text <==> theme().nameProperty

  okButton.disable <== themeNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      theme()
    case _ => null
  }

  onShown = { _ =>
    themeNameField.requestFocus()
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

class ThemeComboBox (subtheme: ObjectProperty[ThematicElementID], story: Narrative) extends ComboBox[ThemeLike] {
  cellFactory = { _ =>
    new JfxListCell[ThemeLike] {
      override def updateItem(item: ThemeLike, empty: Boolean): Unit = {
        super.updateItem(item, empty)

        if (!empty && item != null) {
          setText(item.name)
        }
      }
    }
  }

  converter = new StringConverter[ThemeLike] {
    override def fromString(string: String): ThemeLike = (story.themes find (_.name == string)).get

    override def toString(t: ThemeLike): String = t.name
  }

  onAction = { _ =>
    Logger.info("**** onAction ****")
    Option(value()) foreach { v => Logger.info("ThemeID: " + v.id.toString)
                                   subtheme()=v.id
    }
  }

  value() = (story.themes find (_.id == subtheme())).getOrElse(new UITheme(ThematicElementID(-1),"", List[ThematicElementID](), List[ThematicElementID]()))

  story.themes.map(items() += _)
}

class MotifComboBox (motif: ObjectProperty[ThematicElementID], story: Narrative) extends ComboBox[MotifLike] {
  cellFactory = { _ =>
    new JfxListCell[MotifLike] {
      override def updateItem(item: MotifLike, empty: Boolean): Unit = {
        super.updateItem(item, empty)

        if (!empty && item != null) {
          setText(item.name)
        }
      }
    }
  }

  converter = new StringConverter[MotifLike] {
    override def fromString(string: String): MotifLike = (story.motifs find (_.name == string)).get

    override def toString(t: MotifLike): String = t.name
  }

  onAction = { _ =>
    Logger.info("**** onAction ****")
    Option(value()) foreach { v => Logger.info("MotifID: " + v.id.toString)
      motif()=v.id
    }
  }

  value() = (story.motifs find (_.id == motif())).getOrElse(new UIMotif(ThematicElementID(-1),"", List[String]()))

  story.motifs.map(items() += _)
}