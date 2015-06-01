package org.narrativeandplay.hypedyn.story

trait NarrativeElement[T <: NarrativeElement[T]] {
  self: T =>
}
