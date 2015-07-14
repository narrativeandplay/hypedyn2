package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.Node
import javafx.scene.control.{Skin => JfxSkin, ListCell => JfxListCell}

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos
import scalafx.scene.control._
import scalafx.scene.layout.HBox
import scalafx.util.StringConverter

import org.narrativeandplay.hypedyn.story.UiCondition
import org.narrativeandplay.hypedyn.story.rules.{Or, And, BooleanOperator}

class RuleCellSkin(cell: RuleCell) extends JfxSkin[RuleCell] {
  val rootNode = new TreeView[String]() {
    prefHeight = 200

    root = treeRoot
  }

  lazy val treeRoot = new TreeItem[String]("") {
    graphic = ruleNameField

    children += conditionsNode
    children += new TreeItem[String]("", addCondButton)
    children += actionsNode
    children += new TreeItem[String]("", addActionButton)
  }
  lazy val ruleNameField = new TextField() {
    text <==> cell.rule.nameProperty
  }
  lazy val conditionsNode = new TreeItem[String]("") {
    graphic = new HBox(new Label("If "), conditionCombineType, new Label(" of the following conditions are true:")) {
      alignment = Pos.CenterLeft
    }
  }
  lazy val actionsNode = new TreeItem[String]() {
    value = "Then perform the following actions:"
  }

  lazy val addCondButton = new Button("Add condition") {
    onAction = { _ =>
      println("hello")

      val newCond = new UiCondition("NodeCondition", Map.empty)

      cell.rule.conditionsProperty += newCond
      conditionsNode.children += new RuleCell.ConditionCell(newCond, cell.conditionDefs, cell.story)
    }
  }
  lazy val addActionButton = new Button("Add action") {

  }

  lazy val conditionCombineType = new ComboBox[BooleanOperator]() {
    cellFactory = { _ =>
      new JfxListCell[BooleanOperator] {
        override def updateItem(item: BooleanOperator, empty: Boolean): Unit = {
          super.updateItem(item, empty)

          text = if (!empty && item != null) {
            item match {
              case And => "All"
              case Or => "Any"
            }
          }
          else ""
        }

        // <editor-fold="Functions for replicating Scala-like access style">

        def text = getText
        def text_=(s: String) = setText(s)

        // </editor-fold>
      }
    }

    items = ObservableBuffer(And, Or)
    value <==> cell.rule.conditionsOpProperty

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

  override def dispose(): Unit = {}

  override def getSkinnable: RuleCell = cell

  override def getNode: Node = rootNode
}
