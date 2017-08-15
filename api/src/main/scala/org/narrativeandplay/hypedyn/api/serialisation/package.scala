package org.narrativeandplay.hypedyn.api

package object serialisation {
  /**
   * Define an AST field, i.e., a key-value pair, as a pair of a `String` and `AstElement`
   */
  type AstField = (String, AstElement)
}
