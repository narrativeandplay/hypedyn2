package org.narrativeandplay.hypedyn.dialogs

import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.{control => jfxsc}

import scala.util.Try

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafx.stage.{Modality, Window}
import scalafx.util.StringConverter

import org.tbee.javafx.scene.layout.MigPane

import org.narrativeandplay.hypedyn.story.rules._

class FactEditor private (dialogTitle: String,
                          availableFactTypes: List[String],
                          factToEdit: Option[Fact],
                          ownerWindow: Window) extends Dialog[Fact] {
  def this(dialogTitle: String, availableFactTypes: List[String], ownerWindow: Window) =
    this(dialogTitle, availableFactTypes, None, ownerWindow)

  def this(dialogTitle: String, availableFactTypes: List[String], factToEdit: Fact, ownerWindow: Window) =
    this(dialogTitle, availableFactTypes, Some(factToEdit), ownerWindow)

  title = dialogTitle
  headerText = None

  initOwner(ownerWindow)

  dialogPane().buttonTypes.addAll(ButtonType.OK, ButtonType.Cancel)
  val okButton = dialogPane().lookupButton(ButtonType.OK)
  okButton.disable = true

  private val converter = new StringConverter[BigInt] {
    override def fromString(string: String): BigInt = Try(BigInt(string)) getOrElse BigInt(0)

    override def toString(t: BigInt): String = t.toString()
  }

  private val factNameField = new TextField()
  private val stringFactInitValueField = new TextField()
  private val integerFactInitValueField = new Spinner[BigInt]() {
    editable = true
    valueFactory = new jfxsc.SpinnerValueFactory[BigInt]() {
      setValue(0)
      setConverter(converter)

      override def increment(steps: Int): Unit = setValue(getValue + steps)

      override def decrement(steps: Int): Unit = setValue(BigInt(0).max(getValue - steps))
    }
  }
  private val booleanFactInitValueField = new ComboBox[Boolean]() {
    items = ObservableBuffer(true, false)
    value = false
  }
  private val factTypesField = new ComboBox[String]() {
    promptText = "Choose a fact type"
    items = ObservableBuffer(availableFactTypes)
    factToEdit foreach { fact =>
      fact match {
        case _: StringFact => value = Fact.StringFact
        case _: IntegerFact => value = Fact.IntegerFact
        case _: BooleanFact => value = Fact.BooleanFact
        case f => throw new IllegalArgumentException(s"Unknown or not enabled fact type: $f")
      }

      okButton.disable = false
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
      case "String fact" => contentPane.add(stringFactInitValueField)
      case "Number fact" => contentPane.add(integerFactInitValueField)
      case "True/false fact" => contentPane.add(booleanFactInitValueField)
      case _ => println("error")
    }

    okButton.disable = false
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

  okButton.addEventFilter(ActionEvent.Action, new EventHandler[jfxe.ActionEvent] {
    override def handle(event: jfxe.ActionEvent): Unit = {
      if (factNameField.text().isEmpty) {
        new Alert(Alert.AlertType.Error) {
          initOwner(ownerWindow)
          headerText = None
          contentText = "Facts cannot have an empty name"
        }.showAndWait()
        event.consume()
      }
    }
  })

  resultConverter = {
    case ButtonType.OK =>
      factTypesField.value() match {
        case "String fact" => StringFact(factToEdit map (_.id) getOrElse FactId(-1),
                                         factNameField.text(),
                                         stringFactInitValueField.text())
        case "Number fact" => IntegerFact(factToEdit map (_.id) getOrElse FactId(-1),
                                          factNameField.text(),
                                          integerFactInitValueField.value())
        case "True/false fact" => BooleanFact(factToEdit map (_.id) getOrElse FactId(-1),
                                              factNameField.text(),
                                              booleanFactInitValueField.value())
        case _ => throw new IllegalArgumentException("Invalid fact type")
      }
    case _ => null
  }

  def showAndWait(): Option[Fact] = {
    initModality(Modality.APPLICATION_MODAL)

    val result = delegate.showAndWait()

    if (result.isPresent) Some(result.get()) else None
  }
}
