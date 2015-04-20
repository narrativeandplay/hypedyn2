package org.narrativeandplay.hypedyn.keycombinations

import scalafx.scene.input.{KeyCombination, KeyCode, KeyCodeCombination}

object KeyCombinations {
  val Undo = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown)
  val RedoUnix = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown, KeyCombination.ShiftDown)
  val RedoWin = new KeyCodeCombination(KeyCode.Y, KeyCombination.ShortcutDown)
}
