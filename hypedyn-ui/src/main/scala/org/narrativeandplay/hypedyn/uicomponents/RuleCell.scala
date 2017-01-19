package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{ListCell => JfxListCell}

import org.narrativeandplay.hypedyn.uicomponents.RuleCellComponents.ConditionCell

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.layout.{Priority, HBox, StackPane}
import scalafx.util.StringConverter
import scalafx.scene.Parent.sfxParent2jfx

import org.narrativeandplay.hypedyn.story.rules.BooleanOperator.{Or, And}
import org.narrativeandplay.hypedyn.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.story.{UiAction, UiCondition, UiStory, UiRule}
import org.narrativeandplay.hypedyn.story.rules.{BooleanOperator, ActionDefinition, ConditionDefinition}
import org.narrativeandplay.hypedyn.utils.ExpandableEmptySpace
import org.narrativeandplay.hypedyn.utils.Scala2JavaFunctionConversions._

/**
 * Cell in a rule list to manipulate rules
 *
 * @param rule The rule to manipulate
 * @param conditionDefs The list of condition definitions
 * @param actionDefs The list of action definitions
 * @param story The story the rule belongs to
 * @param ruleList The list of rules the rule belongs to
 */
class RuleCell(val rule: UiRule,
               val conditionDefs: List[ConditionDefinition],
               val actionDefs: List[ActionDefinition],
               val story: ObjectProperty[UiStory],
               val ruleList: ObservableBuffer[UiRule],
               val parentPane: RulesPane) extends TreeItem[String]("") {
  private val self = this
  private val root = self.parent()
  private val moveUpButton = new Button("↑") {
    disable = ruleList(0) == rule

    onAction = { _ =>
      val currentIndex = ruleList indexOf rule
      val newIndex = currentIndex - 1

      val itemToMoveDown = ruleList(newIndex)

      ruleList.set(newIndex, rule)
      ruleList.set(currentIndex, itemToMoveDown)

      parentPane.rearrangeCells((currentIndex, newIndex))
    }
  }
  private val moveDownButton = new Button("↓") {
    disable = ruleList.last == rule

    onAction = { _ =>
      val currentIndex = ruleList indexOf rule
      val newIndex = currentIndex + 1

      val itemToMoveUp = ruleList(newIndex)

      ruleList.set(newIndex, rule)
      ruleList.set(currentIndex, itemToMoveUp)

      parentPane.rearrangeCells((currentIndex, newIndex))
    }
  }

  ruleList onChange { (buffer, changes) =>
    moveUpButton.disable = buffer.head == rule
    moveDownButton.disable = buffer.last == rule
  }

  graphic = ruleNameField

  expanded = true

  children += conditionsNode
  children += actionsNode

  lazy val ruleNameField = new HBox(10) {
    alignment = Pos.CenterLeft

    children += moveUpButton
    children += moveDownButton
    children += new Button("−") {
      onAction = { _ =>
        ruleList -= rule
        self.parent().children -= self
      }
    }
    children += new TextField() {
      text <==> rule.nameProperty

      HBox.setHgrow(this, Priority.Always)
    }
    children += new CheckBox("Stop if true") {
      allowIndeterminate = false

      selected <==> rule.stopIfTrueProperty
    }
  }
  lazy val conditionsNode = new TreeItem[String]("") {
    expanded = true

    graphic = new HBox {
      children += new Label("If ") {
        minWidth = 20
      }
      children += conditionCombineType
      children += new Label(" of the following conditions are true:") {
        minWidth = 200
      }
      children += addCondButton

      alignment = Pos.CenterLeft
    }
  }
  lazy val actionsNode = new TreeItem[String]("") {
    expanded = true

    graphic = new HBox {
      children += new Label("Then perform the following actions:")
      children += addActionButton

      alignment = Pos.CenterLeft
    }
  }

  lazy val addCondButton: StackPane = new StackPane {
    padding = Insets(0, 0, 0, 10)
    children += new Button("Add condition") {
      onAction = { _ =>
        val newCond = addCondition()
        conditionsNode.children += new ConditionCell(newCond, conditionDefs, story, self, rule)
      }
    }
  }
  lazy val addActionButton: StackPane = new StackPane {
    padding = Insets(0, 0, 0, 10)
    children += new Button("Add action") {
      onAction = { _ =>
        val newAction = addAction()
        actionsNode.children += new RuleCellComponents.ActionCell(newAction, actionDefs, story, self, rule)
      }
    }
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
    value <==> rule.conditionsOpProperty

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

  rule.conditions foreach { condition =>
    conditionsNode.children += new RuleCellComponents.ConditionCell(condition, conditionDefs, story, this, rule)
  }
  rule.actions foreach { action =>
    actionsNode.children += new RuleCellComponents.ActionCell(action, actionDefs, story, this, rule)
  }

  /**
   * Add a condition to the rule
   *
   * @return The added condition
   */
  def addCondition(): UiCondition = {
    val newCond = new UiCondition(ConditionType("NodeCondition"), Map.empty)
    rule.conditionsProperty() += newCond
    newCond
  }

  /**
   * Add an action to the rule
   *
   * @return The added action
   */
  def addAction(): UiAction = {
    val newAction = new UiAction(actionDefs.head.actionType, Map.empty)
    rule.actionsProperty() += newAction
    newAction
  }

  def rearrangeConditions(): Unit = {
    conditionsNode.children.clear()
    rule.conditions foreach { condition =>
      conditionsNode.children += new RuleCellComponents.ConditionCell(condition, conditionDefs, story, this, rule)
    }
  }

  def rearrangeActions(): Unit = {
    actionsNode.children.clear()
    rule.actions foreach { action =>
      actionsNode.children += new RuleCellComponents.ActionCell(action, actionDefs, story, this, rule)
    }
  }
}
