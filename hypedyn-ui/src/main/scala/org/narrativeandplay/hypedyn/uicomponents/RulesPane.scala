package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.Node
import javafx.scene.control.{Skin, ListCell => JFXListCell}

import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.layout.HBox
import scalafx.scene.Parent.sfxParent2jfx // Following import required because ScalaFX can't be bothered to fix some basic conversion issues

import org.narrativeandplay.hypedyn.story.UiRule
import org.narrativeandplay.hypedyn.story.rules._

class RulesPane(val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                initRules: List[RuleLike]) extends ListView[RuleLike] {
  private val _rules = ArrayBuffer(initRules: _*)

  items = ObservableBuffer(_rules)

  cellFactory = { _ =>
    new RulesPane.RulesPaneCell
  }

  def rules = _rules.toList
}

object RulesPane {
  private class RulesPaneCell extends JFXListCell[RuleLike] {
    setSkin(new RulesPaneCellSkin(this))
  }

  private class RulesPaneCellSkin(cell: JFXListCell[RuleLike]) extends Skin[JFXListCell[RuleLike]] {
    private val root = new TreeView[String]() {
      root = treeRoot
      showRoot = true
    }

    private lazy val treeRoot = new TreeItem[String]() {
      graphic = new TextField() {
        text = "New Rule"
      }
      value = ""

      children += conditionsNode
      children += actionsNode
    }

    private lazy val conditionsNode = new TreeItem[String]() {
      graphic = new HBox() {
        children += new Label("If ")
        children += new ComboBox[BooleanOperator]() {
          cellFactory = { _ =>
            new ListCell[BooleanOperator] {
              text = item() match {
                case And => "All"
                case Or => "Any"
                case _ => "..."
              }
            }
          }
          items = ObservableBuffer(And, Or)
          value = Option(cell.itemProperty().get()) map (_.conditionsOp) getOrElse Or
        }
        children += new Label(" of the following conditions are true:")
      }

      value = ""
    }

    private lazy val actionsNode = new TreeItem[String]() {
      value = "Then perform the following actions:"
    }

    override def dispose(): Unit = {}

    override def getSkinnable: JFXListCell[RuleLike] = cell

    override def getNode: Node = root
  }
}
