package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{ListCell => JFXListCell}

import scala.language.implicitConversions

import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._

import org.narrativeandplay.hypedyn.story.{UiStory, UiRule}
import org.narrativeandplay.hypedyn.story.rules._

class RulesPane(val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                val rules: ObservableBuffer[UiRule],
                val story: ObjectProperty[UiStory]) extends ListView[UiRule] {

  items = rules

  cellFactory = { _ =>
    new RulesPane.RulesPaneCell(this)
  }

  def addRule(): Unit = {
    rules += new UiRule(RuleId(-1), "New Rule", Or, Nil, Nil)
  }
}

object RulesPane {
  private class RulesPaneCell(parentView: RulesPane) extends JFXListCell[UiRule] {
    setPadding(Insets.Empty) // fill the whole cell
    private val self = this

    override def updateItem(item: UiRule, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      if (empty || item == null) {
        setGraphic(null)
      }
      else {
        setGraphic(new RuleCell(item, parentView.conditionDefinitions, parentView.actionDefinitions, parentView.story))
      }
    }
  }
}
