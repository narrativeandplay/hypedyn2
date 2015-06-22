package org.narrativeandplay.hypedyn.uicomponents

import javafx.collections.ObservableList
import javafx.scene.control.{ListCell, ListView}
import javafx.util.Callback

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Tooltip

import org.narrativeandplay.hypedyn.story.rules.Fact

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

  def add(fact: Fact) = facts += fact
  def remove(fact: Fact) = facts -= fact
  def update(fact: Fact, newFact: Fact) = facts find (_ == fact) foreach { _ =>
    facts.update(facts indexOf fact, newFact)
  }

  private val deselectionInfo = new Tooltip() {
    text = s"${if (System.getProperty("os.name").toLowerCase == "mac os x") "Cmd" else "Ctrl"}-Click deselects a selected fact"
  }

  tooltip = deselectionInfo

//  focusedProperty().addListener({ (_, _, selected) =>
//    if (!selected) selectionModel.clearSelection()
//  })

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

  def selectionModel = getSelectionModel

  def tooltip = getTooltip
  def tooltip_=(tooltip: Tooltip) = setTooltip(tooltip)

  //</editor-fold>
}
