package org.narrativeandplay.hypedyn.story.rules

trait Ruleset[T] {
  def parent: T
  def name: Option[String]
  def rules: List[Rule[T]]
}
