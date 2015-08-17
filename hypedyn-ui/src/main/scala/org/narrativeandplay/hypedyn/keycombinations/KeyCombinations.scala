package org.narrativeandplay.hypedyn.keycombinations

import scalafx.scene.input.{KeyCombination, KeyCode, KeyCodeCombination}

/**
 * List of shortcut key combinations
 */
object KeyCombinations {
  val Undo = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown)
  val RedoUnix = new KeyCodeCombination(KeyCode.Z, KeyCombination.ShortcutDown, KeyCombination.ShiftDown)
  val RedoWin = new KeyCodeCombination(KeyCode.Y, KeyCombination.ShortcutDown)

  val Cut = new KeyCodeCombination(KeyCode.X, KeyCombination.ShortcutDown)
  val Copy = new KeyCodeCombination(KeyCode.C, KeyCombination.ShortcutDown)
  val Paste = new KeyCodeCombination(KeyCode.V, KeyCombination.ShortcutDown)

  val New = new KeyCodeCombination(KeyCode.N, KeyCombination.ShortcutDown)
  val Open = new KeyCodeCombination(KeyCode.O, KeyCombination.ShortcutDown)
  val Save = new KeyCodeCombination(KeyCode.S, KeyCombination.ShortcutDown)
  val SaveAs = new KeyCodeCombination(KeyCode.S, KeyCombination.ShortcutDown, KeyCombination.ShiftDown)
}
