package org.narrativeandplay.hypedyn.uicomponents

import java.lang
import javafx.beans.value.ObservableValue
import javafx.stage.Window
import javafx.{event => jfxe}
import javafx.event.EventHandler
import javafx.scene.control.{ListCell => JfxListCell}
import javafx.scene.{input => jfxsi, Scene}

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.events.UiEventDispatcher

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.Event
import scalafx.scene.control.{ListView, Tooltip}

import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.utils.System

import scalafx.scene.input.MouseEvent

/**
 * List view for facts
 */
object FactViewer extends ListView[Fact] {
  val facts = new ObservableBuffer[Fact]()

  items = facts

  class FactCell extends JfxListCell[Fact] {
    override def updateItem(item: Fact, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      text = Option(item) map (_.name) getOrElse ""
    }

    onMouseClicked = { me =>
      if (me.clickCount == 2) {
        Option(itemProperty().get()) foreach { fact => UiEventDispatcher.requestEditFact(fact) }
      }
    }

    // <editor-fold="Functions for replicating Scala-like access style">

    def text = textProperty()
    def text_=(newText: String) = setText(newText)

    def onMouseClicked = { me: MouseEvent => getOnMouseClicked.handle(me) }
    def onMouseClicked_=[T >: MouseEvent <: Event, U >: jfxsi.MouseEvent <: jfxe.Event](lambda: T => Unit)(implicit jfx2sfx: U => T) = {
      setOnMouseClicked(new EventHandler[U] {
        override def handle(event: U): Unit = lambda(event)
      })
    }

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

  EasyBind select scene select[Window] (_.window) selectObject[lang.Boolean] (_.focused) onChange { (_, _, isFocused) =>
    Option(isFocused) foreach { focused =>
      if (focused) tooltip = deselectionInfo else tooltip = null
    }
  }
}
