package org.narrativeandplay.hypedyn.uicomponents

import javafx.util.Callback
import javafx.{scene => jfxs}
import javafx.scene.{control => jfxsc, Node}

import scalafx.Includes._
import scalafx.scene.control.{TextField, TreeItem, TreeView, Label}
import scalafx.scene.layout.VBox

import org.narrativeandplay.hypedyn.story.rules.{RuleLike, ActionDefinition, ConditionDefinition}

class NodeRulesPane(val conditionDefinitions: List[ConditionDefinition],
                    val actionDefinitions: List[ActionDefinition],
                    val rules: List[RuleLike]) extends jfxsc.Control {
  setSkin(new NodeRulesPane.NodeRulesPaneSkin(this))
}

object NodeRulesPane {
  private class NodeRulesPaneSkin(pane: NodeRulesPane) extends jfxsc.Skin[NodeRulesPane] {
    private val root = new VBox() {
      children += new Label("Node Rules")
      children += RulesPane
    }

    private object RulesPane extends jfxsc.ListView[RuleLike] {
      class RulesPaneCell extends jfxsc.ListCell[RuleLike]
      class RulesPaneCellSkin(cell: RulesPaneCell) extends jfxsc.Skin[jfxsc.ListCell[RuleLike]] {
        private val root = new TreeView[String]() {
          root = new TreeItem[String]() {
            graphic = new TextField
          }
        }

        override def dispose(): Unit = {}

        override def getSkinnable: jfxsc.ListCell[RuleLike] = cell

        override def getNode: Node = root
      }

      cellFactory = { _ =>
        new RulesPaneCell
      }

      // <editor-fold="Functions for replicating Scala-like access style">

      def cellFactory = cellFactoryProperty
      def cellFactory_=(v: (jfxsc.ListView[RuleLike] => jfxsc.ListCell[RuleLike])) {
        setCellFactory(new Callback[jfxsc.ListView[RuleLike], jfxsc.ListCell[RuleLike]] {
          override def call(lv: jfxsc.ListView[RuleLike]) = {
            v(lv)
          }
        })
      }

      //</editor-fold>
    }

    override def dispose(): Unit = {}

    override def getSkinnable: NodeRulesPane = pane

    override def getNode: jfxs.Node = root
  }
}