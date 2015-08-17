package org.narrativeandplay.hypedyn.uicomponents

import javafx.collections.ObservableList
import javafx.scene.control.{ListCell, ListView}
import javafx.util.Callback

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{MultipleSelectionModel, Tooltip}

import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.utils.System

/**
 * List view for facts
 */
object FactViewer extends ListView[Fact] {
  val facts = new ObservableBuffer[Fact]()

  items = facts

  class FactCell extends ListCell[Fact] {
    override def updateItem(item: Fact, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      text = Option(item) map (_.name) getOrElse ""
    }

    // <editor-fold="Functions for replicating Scala-like access style">

    def text = textProperty()
    def text_=(newText: String) = setText(newText)

    //</editor-fold>
  }

  cellFactory = { _ =>
    new FactCell
  }

  /**
   * Add a fact to this list view
   *
   * @param fact The fact to add
   */
  def add(fact: Fact): Unit = facts += fact

  /**
   * Remove a fact from this list view
   *
   * @param fact The fact to remove
   */
  def remove(fact: Fact): Unit = facts -= fact

  /**
   * Update a face in this list view
   *
   * @param fact The fact to update
   * @param newFact The updated version of the fact
   */
  def update(fact: Fact, newFact: Fact): Unit = facts find (_ == fact) foreach { _ =>
    facts.update(facts indexOf fact, newFact)
  }

  private val deselectionInfo = new Tooltip() {
    text = s"${if (System.isMac) "Cmd" else "Ctrl"}-Click deselects a selected fact"
  }

  tooltip = deselectionInfo

  // <editor-fold="Functions for replicating Scala-like access style">

  def cellFactory = cellFactoryProperty
  def cellFactory_=(v: (ListView[Fact] => ListCell[Fact])) {
    setCellFactory(new Callback[ListView[Fact], ListCell[Fact]] {
      override def call(lv: ListView[Fact]) = {
        v(lv)
      }
    })
  }

  def items = itemsProperty()
  def items_=(i: ObservableList[Fact]) = setItems(i)

  def selectionModel: MultipleSelectionModel[Fact] = getSelectionModel

  def tooltip: Tooltip = getTooltip
  def tooltip_=(tooltip: Tooltip) = setTooltip(tooltip)

  //</editor-fold>
}
