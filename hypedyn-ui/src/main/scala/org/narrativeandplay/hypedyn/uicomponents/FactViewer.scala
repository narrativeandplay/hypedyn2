package org.narrativeandplay.hypedyn.uicomponents

import java.lang
import javafx.stage.Window

import org.narrativeandplay.hypedyn.events.UiEventDispatcher

import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView, Tooltip}

import org.gerweck.scalafx.util._
import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.story.rules.Fact
import org.narrativeandplay.hypedyn.utils.System

/**
 * List view for facts
 */
object FactViewer extends ListView[Fact] {
  val facts = new ObservableBuffer[Fact]()

  items = facts

  cellFactory = { _ =>
    new ListCell[Fact] {
      item onChange { (_, _, newFactOption) =>
        text = Option(newFactOption) map (_.name) getOrElse ""
      }

      onMouseClicked = { me =>
        if (me.clickCount == 2) {
          Option(item()) foreach UiEventDispatcher.requestEditFact
        }
      }
    }
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

  // The functions `select` and `selectObject` are Java method calls to a generic methods. Due to limitations in the
  // Scala compiler, the type parameters of these method calls must be provided for Scala to correctly type the results.
  EasyBind select scene select[Window] (_.window) selectObject[lang.Boolean] (_.focused) onChange { (_, _, isFocused) =>
    Option(isFocused) foreach { focused =>
      if (focused) tooltip = deselectionInfo else tooltip = null
    }
  }
}
