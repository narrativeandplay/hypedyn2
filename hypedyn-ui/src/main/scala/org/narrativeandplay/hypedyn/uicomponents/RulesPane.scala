package org.narrativeandplay.hypedyn.uicomponents

import java.util.function
import javafx.scene.control.{ListCell => JFXListCell}

import scala.language.implicitConversions
import scala.collection.mutable.ArrayBuffer

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._

import org.narrativeandplay.hypedyn.story.{Narrative, UiRule}
import org.narrativeandplay.hypedyn.story.rules._

class RulesPane(val conditionDefinitions: List[ConditionDefinition],
                val actionDefinitions: List[ActionDefinition],
                initRules: List[UiRule],
                val story: Narrative) extends ListView[UiRule] {

  private val _rules = ArrayBuffer(initRules: _*)

  items = ObservableBuffer(_rules)

  cellFactory = { _ =>
    new RulesPane.RulesPaneCell(this)
  }

  def rules = _rules.toList
}

object RulesPane {
  implicit def function1ToFunction[T, U](f: T => U): java.util.function.Function[T, U] = new function.Function[T, U] {
    override def apply(t: T): U = f(t)
  }

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
