package org.narrativeandplay.hypedyn.uicomponents

import java.util.function
import javafx.event.EventHandler
import javafx.scene.control.{ListCell => JFXListCell, TreeCell => JFXTreeCell}

import scala.language.implicitConversions
import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.HBox
import scalafx.scene.Parent.sfxParent2jfx

import org.fxmisc.easybind.EasyBind

// Following import required because ScalaFX can't be bothered to fix some basic conversion issues
import scalafx.util.StringConverter

import org.narrativeandplay.hypedyn.story.UiRule
import org.narrativeandplay.hypedyn.story.rules._

class RulesPane(val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                initRules: List[UiRule]) extends ListView[UiRule] {

  private val _rules = ArrayBuffer(initRules: _*)

  items = ObservableBuffer(_rules)

  cellFactory = { _ =>
    new RulesPane.RulesPaneCell
  }

  def rules = _rules.toList
}

object RulesPane {
  implicit def function1ToFunction[T, U](f: T => U): java.util.function.Function[T, U] = new function.Function[T, U] {
    override def apply(t: T): U = f(t)
  }

  private class RulesPaneCell extends JFXListCell[UiRule] {
    setPadding(Insets.Empty) // fill the whole cell
    private val self = this

    override def updateItem(item: UiRule, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      if (empty || item == null) {
        setGraphic(null)
      }
      else {
        val root = new TreeView[String]() {
          prefHeight = 100


        }
        root.root = treeRoot

        lazy val treeRoot: TreeItem[String] = new TreeItem[String]() {
          graphic = new TextField() {
            text <==> itemProperty().get().nameProperty
          }
          value = ""

          children += conditionsNode
          children += new TreeItem[String]("", condButton)
          children += actionsNode
          children += new TreeItem[String]("", new Button("Add action"))
        }

        lazy val condButton = new Button("Add condition") {
          onAction = new EventHandler[javafx.event.ActionEvent] {
            override def handle(ae: javafx.event.ActionEvent): Unit = println(getItem)
          }
        }

        lazy val conditionCombineType = new ComboBox[BooleanOperator]() {
          cellFactory = { _ =>
            new JFXListCell[BooleanOperator] {
              override def updateItem(item: BooleanOperator, empty: Boolean): Unit = {
                super.updateItem(item, empty)

                text = ""

                text = Option(item) match {
                  case None => ""
                  case Some(i) => i match {
                    case And => "All"
                    case Or => "Any"
                  }
                }
              }

              // <editor-fold="Functions for replicating Scala-like access style">

              def text = getText
              def text_=(s: String) = setText(s)

              // </editor-fold>
            }
          }

          items = ObservableBuffer(And, Or)
          value = Or

          converter = new StringConverter[BooleanOperator] {
            override def fromString(string: String): BooleanOperator = string match {
              case "Any" => Or
              case "All" => And
              case s => throw new IllegalArgumentException(s"Illegal BooleanOperator type: $s")
            }

            override def toString(t: BooleanOperator): String = t match {
              case And => "All"
              case Or => "Any"
            }
          }
        }
        lazy val conditionsNode = new TreeItem[String]() {
          graphic = new HBox() {
            children += new Label("If ")
            children += conditionCombineType
            children += new Label(" of the following conditions are true:")
          }

          value = ""

          children += new TreeItem[String]("")
        }

        lazy val actionsNode = new TreeItem[String]() {
          value = "Then perform the following actions:"

          children += new TreeItem[String]("")
        }

        setGraphic(root)
        conditionCombineType.value = item.conditionsOp
      }
    }

    //def isCollapsed = root.expandedItemCount() == 1
  }
}
