package org.narrativeandplay.hypedyn.utils

import scalafx.scene.layout.{Priority, HBox}

class ExpandableEmptySpace extends HBox {
  HBox.setHgrow(this, Priority.Always)
}
