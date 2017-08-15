package org.narrativeandplay.hypedyn.ui.dialogs

import javafx.scene.{control => jfxsc}

import scala.util.Try

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.stage.{Modality, Window}
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx

import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.api.story.rules._

/**
 * Dialog for editing facts
 *
 * @param dialogTitle The title of the dialog
 * @param availableFactTypes The fact types available for creation
 * @param factToEdit An option containing the fact to edit, or None if a new fact is to be created
 * @param ownerWindow The parent window of the dialog, for inheriting icons
 */
class FactEditor private (dialogTitle: String,
                          availableFactTypes: List[String],
                          factToEdit: Option[Fact],
                          ownerWindow: Window) extends Dialog[Fact] {
  /**
   * Creates a new dialog for creating a fact
   *
   * @param dialogTitle The title of the dialog
   * @param availableFactTypes The fact types available for creation
   * @param ownerWindow The parent window of the dialog, for inheriting icons
   * @return A new fact dialog
   */
  def this(dialogTitle: String, availableFactTypes: List[String], ownerWindow: Window) =
    this(dialogTitle, availableFactTypes, None, ownerWindow)

  /**
   * Creates a new dialog for editing a fact
   *
   * @param dialogTitle The title of the dialog
   * @param availableFactTypes The fact types available for creation
   * @param factToEdit The fact to edit
   * @param ownerWindow The parent window of the dialog, for inheriting icons
   * @return A new fact dialog
   */
  def this(dialogTitle: String, availableFactTypes: List[String], factToEdit: Fact, ownerWindow: Window) =
    this(dialogTitle, availableFactTypes, Some(factToEdit), ownerWindow)

  title = dialogTitle
  headerText = None

  initOwner(ownerWindow)

  // HACK: make fact editor always on top
  // Possibly due to some touch screen issues
  dialogPane().scene().window().asInstanceOf[javafx.stage.Stage].alwaysOnTop = true

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)

  private val factNameField = new TextField()
  private val stringFactInitValueField = new TextField()
  private val integerFactInitValueField = new Spinner[BigInt]() {
    editable = true
    valueFactory = new jfxsc.SpinnerValueFactory[BigInt]() {
      setValue(0)
      setConverter(new StringConverter[BigInt] {
        override def fromString(string: String): BigInt = Try(BigInt(string)) getOrElse BigInt(0)

        override def toString(t: BigInt): String = t.toString()
      })

      override def increment(steps: Int): Unit = setValue(getValue + steps)

      override def decrement(steps: Int): Unit = setValue(getValue - steps)
    }

    editor().text onChange { (_, _, _) =>
      valueFactory().value = valueFactory().converter().fromString(editor().text())
    }
  }
  private val booleanFactInitValueField = new ComboBox[Boolean]() {
    items = ObservableBuffer(true, false)
    value = false
  }
  private val factTypesField = new ComboBox[String]() {
    promptText = "Choose a fact type"
    items = ObservableBuffer(availableFactTypes)
    factToEdit foreach {
      case _: StringFact => value = Fact.StringFact
      case _: IntegerFact => value = Fact.IntegerFact
      case _: BooleanFact => value = Fact.BooleanFact
      case f => throw new IllegalArgumentException(s"Unknown or not enabled fact type: $f")
    }
  }

  private val initValLabel = new Label("Initial value: ")

  private val contentPane: MigPane = new MigPane("fill") {
    add(new Label("Fact Type: "))
    add(factTypesField, "wrap")
    add(new Label("Name: "))
    add(factNameField, "wrap")
  }
  dialogPane().content = contentPane

  factTypesField.onAction = { _ =>
    if (!contentPane.getChildren.contains(initValLabel)) {
      contentPane.add(initValLabel)
    }

    contentPane.remove(integerFactInitValueField)
    contentPane.remove(booleanFactInitValueField)
    contentPane.remove(stringFactInitValueField)

    factTypesField.value() match {
      case Fact.StringFact => contentPane.add(stringFactInitValueField)
      case Fact.IntegerFact => contentPane.add(integerFactInitValueField)
      case Fact.BooleanFact => contentPane.add(booleanFactInitValueField)
      case _ => println("error")
    }

    dialogPane().getScene.getWindow.sizeToScene()
  }

  factToEdit foreach { f =>
    contentPane.add(initValLabel)

    factNameField.text = f.name

    f match {
      case s: StringFact =>
        contentPane.add(stringFactInitValueField)
        stringFactInitValueField.text = s.initialValue
      case i: IntegerFact =>
        contentPane.add(integerFactInitValueField)
        integerFactInitValueField.valueFactory().value = i.initalValue
      case b: BooleanFact =>
        contentPane.add(booleanFactInitValueField)
        booleanFactInitValueField.value = b.initialValue
      case unknown => throw new IllegalArgumentException(s"Unenabled fact type: $unknown")
    }
  }

  okButton.disable <== factTypesField.selectionModel().selectedItem.isNull || factNameField.text.isEmpty

  resultConverter = {
    case ButtonType.OK =>
      factTypesField.value() match {
        case Fact.StringFact => StringFact(factToEdit map (_.id) getOrElse FactId(-1),
                                           factNameField.text(),
                                           stringFactInitValueField.text())
        case Fact.IntegerFact => IntegerFact(factToEdit map (_.id) getOrElse FactId(-1),
                                             factNameField.text(),
                                             integerFactInitValueField.value())
        case Fact.BooleanFact => BooleanFact(factToEdit map (_.id) getOrElse FactId(-1),
                                             factNameField.text(),
                                             booleanFactInitValueField.value())
        case _ => throw new IllegalArgumentException("Invalid fact type")
      }
    case _ => null
  }

  /**
   * Shows a blocking fact editor dialog
   *
   * @return An option containing the result of the dialog, or None if the dialog was not closed using
   *         the OK button
   */
  def showAndWait(): Option[Fact] = {
    initModality(Modality.ApplicationModal)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
