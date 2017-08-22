package org.narrativeandplay.hypedyn.ui.components

import java.lang
import javafx.stage.Window

import org.fxmisc.easybind.EasyBind

import org.narrativeandplay.hypedyn.ui.events.UiEventDispatcher
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ListCell, ListView, Tooltip}

import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.api.utils.System

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
    text = s"${if (System.IsMac) "Cmd" else "Ctrl"}-Click deselects a selected fact"
  }
}
