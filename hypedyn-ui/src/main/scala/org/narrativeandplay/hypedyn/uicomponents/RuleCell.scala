package org.narrativeandplay.hypedyn.uicomponents

import javafx.scene.control.{Control => JfxControl, SpinnerValueFactory => JfxSpinnerValueFactory, ListCell => JfxListCell}

import scala.util.Try

import scalafx.Includes._
import scalafx.collections.{ObservableMap, ObservableBuffer}
import scalafx.scene.control.{ComboBox, TreeItem, Spinner, TextArea}
import scalafx.scene.layout.HBox
import scalafx.util.StringConverter
import scalafx.util.StringConverter.sfxStringConverter2jfx

import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.{UiCondition, Narrative, Nodal, UiRule}

class RuleCell(val rule: UiRule,
               val conditionDefs: List[ConditionDefinition],
               val actionDefs: List[ActionDefinition],
               val story: Narrative) extends JfxControl {
  setSkin(new RuleCellSkin(this))

  }

  }
}
