package org.narrativeandplay.hypedyn.keycombinations

import scalafx.scene.input.{KeyCombination, KeyCode, KeyCodeCombination}

object KeyCombinations {
  val Undo = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown)
  val RedoUnix = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown, KeyCombination.ShiftDown)
  val RedoWin = new KeyCodeCombination(KeyCode.Y, KeyCombination.ShortcutDown)

  val Cut = new KeyCodeCombination(KeyCode.X, KeyCombination.ShortcutDown)
  val Copy = new KeyCodeCombination(KeyCode.C, KeyCombination.ShortcutDown)
  val Paste = new KeyCodeCombination(KeyCode.V, KeyCombination.ShortcutDown)
}
